package oficina.mecanica.backendOficina.notificacao.usecase;

import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.MensagemNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.Telefone;
import oficina.mecanica.backendOficina.notificacao.usecase.port.AgendadorDeLembrete;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

public class AgendarLembreteRevisaoUseCase {

    public static final int DIAS_ANTECEDENCIA = 7;

    private final EnviadorDeMensagem enviador;
    private final NotificacaoGateway gateway;
    private final AgendadorDeLembrete agendador;
    private final Clock clock;

    public AgendarLembreteRevisaoUseCase(EnviadorDeMensagem enviador,
                                         NotificacaoGateway gateway,
                                         AgendadorDeLembrete agendador,
                                         Clock clock) {
        this.enviador = enviador;
        this.gateway = gateway;
        this.agendador = agendador;
        this.clock = clock;
    }

    public ResultadoNotificacao executar(OrdemParaNotificar ordem) {
        Long ordemId = ordem.id();

        if (ordem.dataProximaRevisao() == null) {
            cancelar(ordemId);
            return ResultadoNotificacao.naoEnviada("Data de revisão preventiva não informada.");
        }

        if (ordemId != null && agendador.foiProcessado(ordemId, ordem.dataProximaRevisao())) {
            return ResultadoNotificacao.naoEnviada("Lembrete de revisão já processado nesta execução.");
        }

        LocalDateTime dataEnvio = ordem.dataProximaRevisao().minusDays(DIAS_ANTECEDENCIA);
        LocalDateTime agora = LocalDateTime.now(clock);

        if (!dataEnvio.isAfter(agora)) {
            cancelar(ordemId);
            return enviarAgora(ordem, agora);
        }

        if (ordemId != null && agendador.estaAgendado(ordemId, dataEnvio)) {
            return ResultadoNotificacao.agendada(dataEnvio, "Lembrete de revisão já estava agendado.");
        }

        // Data mudou (ou nunca foi agendado): o agendamento antigo sai e o
        // lembrete pendente existente é reaproveitado com a nova data.
        if (ordemId != null) {
            agendador.cancelar(ordemId);
        }

        Long notificacaoId = registrarOuAtualizarPendente(ordem, dataEnvio);
        agendador.agendar(ordemId, dataEnvio, () -> enviarAgendado(ordem, notificacaoId));

        return ResultadoNotificacao.agendada(dataEnvio, "Lembrete de revisão preventiva agendado.");
    }

    /** Cancela o lembrete da OS (revisão removida, OS reaberta ou cancelada). */
    public void cancelar(Long ordemId) {
        if (ordemId == null) {
            return;
        }

        agendador.cancelar(ordemId);
        gateway.descartarLembretesPendentes(ordemId);
    }

    private ResultadoNotificacao enviarAgora(OrdemParaNotificar ordem, LocalDateTime agora) {
        String mensagem = mensagem(ordem);
        String telefone = Telefone.normalizar(ordem.telefoneCliente());

        if (telefone == null) {
            registrarImediata(ordem, mensagem, StatusNotificacao.erro);
            return ResultadoNotificacao.naoEnviada("Lembrete de revisão não registrado: cliente sem telefone cadastrado.");
        }

        Long notificacaoId = registrarImediata(ordem, mensagem, StatusNotificacao.pendente);

        if (!enviador.enviar(notificacaoId, telefone, mensagem, mensagemCurta(ordem), contexto(ordem))) {
            marcarErro(notificacaoId);
            return ResultadoNotificacao.naoEnviada("Lembrete de revisão não enviado: serviço de mensagens indisponível.");
        }

        if (ordem.id() != null) {
            agendador.marcarProcessado(ordem.id(), ordem.dataProximaRevisao());
        }

        return ResultadoNotificacao.enviadaImediatamente(
                agora, "Data da revisão com menos de " + DIAS_ANTECEDENCIA + " dias; lembrete registrado imediatamente.");
    }

    /** Executado pelo agendador na data prevista. */
    private void enviarAgendado(OrdemParaNotificar ordemAgendada, Long notificacaoId) {
        OrdemParaNotificar ordem = ordemAgendada;

        if (ordemAgendada.id() != null) {
            ordem = gateway.buscarOrdemFinalizada(ordemAgendada.id()).orElse(null);

            if (ordem == null) {
                // OS excluída, reaberta ou cancelada depois do agendamento.
                gateway.descartarLembretesPendentes(ordemAgendada.id());
                return;
            }

            if (!Objects.equals(ordem.dataProximaRevisao(), ordemAgendada.dataProximaRevisao())) {
                // A data mudou; o novo agendamento cuida do envio.
                return;
            }
        }

        String telefone = Telefone.normalizar(ordem.telefoneCliente());
        boolean aceita = telefone != null
                && enviador.enviar(notificacaoId, telefone, mensagem(ordem), mensagemCurta(ordem), contexto(ordem));

        if (!aceita) {
            marcarErro(notificacaoId);
            return;
        }

        if (ordem.id() != null) {
            agendador.marcarProcessado(ordem.id(), ordem.dataProximaRevisao());
        }
    }

    private Long registrarImediata(OrdemParaNotificar ordem, String mensagem, StatusNotificacao status) {
        if (ordem.clienteId() == null) {
            return null;
        }

        return gateway.registrar(new NovaNotificacao(
                ordem.clienteId(),
                ordem.veiculoId(),
                ordem.id(),
                TipoNotificacao.revisao_preventiva,
                MensagemNotificacao.ASSUNTO_REVISAO,
                mensagem,
                status,
                ordem.telefoneCliente(),
                null,
                null
        )).orElse(null);
    }

    private Long registrarOuAtualizarPendente(OrdemParaNotificar ordem, LocalDateTime dataAgendamento) {
        String mensagem = mensagem(ordem);

        if (ordem.id() != null) {
            Long atualizado = gateway
                    .atualizarLembreteRevisaoPendente(ordem.id(), mensagem, dataAgendamento)
                    .orElse(null);
            if (atualizado != null) {
                return atualizado;
            }
        }

        if (ordem.clienteId() == null) {
            return null;
        }

        return gateway.registrar(new NovaNotificacao(
                ordem.clienteId(),
                ordem.veiculoId(),
                ordem.id(),
                TipoNotificacao.revisao_preventiva,
                MensagemNotificacao.ASSUNTO_REVISAO,
                mensagem,
                StatusNotificacao.pendente,
                ordem.telefoneCliente(),
                dataAgendamento,
                null
        )).orElse(null);
    }

    private void marcarErro(Long notificacaoId) {
        if (notificacaoId != null) {
            gateway.registrarResultadoEnvio(notificacaoId, false, null);
        }
    }

    private String contexto(OrdemParaNotificar ordem) {
        return "lembrete de revisão da OS " + ordem.id();
    }

    private String mensagem(OrdemParaNotificar ordem) {
        return MensagemNotificacao.lembreteRevisao(
                ordem.nomeCliente(), ordem.modeloVeiculo(), ordem.placaVeiculo(), ordem.dataProximaRevisao());
    }

    private String mensagemCurta(OrdemParaNotificar ordem) {
        return MensagemNotificacao.lembreteRevisaoCurto(
                ordem.nomeCliente(), ordem.modeloVeiculo(), ordem.placaVeiculo(), ordem.dataProximaRevisao());
    }
}