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

/**
 * Regra do lembrete de revisão preventiva: o cliente é avisado
 * {@value #DIAS_ANTECEDENCIA} dias antes da data prevista. Se a data já está
 * dentro desse prazo, o lembrete é enviado imediatamente; caso contrário, é
 * registrado como pendente e agendado.
 */
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
        if (ordem.dataProximaRevisao() == null) {
            return ResultadoNotificacao.naoEnviada("Data de revisão preventiva não informada.");
        }

        Long ordemId = ordem.id();
        if (ordemId != null && agendador.foiProcessado(ordemId)) {
            return ResultadoNotificacao.naoEnviada("Lembrete de revisão já processado nesta execução.");
        }

        LocalDateTime dataEnvio = ordem.dataProximaRevisao().minusDays(DIAS_ANTECEDENCIA);
        LocalDateTime agora = LocalDateTime.now(clock);

        if (!dataEnvio.isAfter(agora)) {
            return enviarAgora(ordem, agora);
        }

        if (ordemId != null && agendador.estaAgendado(ordemId)) {
            return ResultadoNotificacao.agendada(dataEnvio, "Lembrete de revisão já estava agendado.");
        }

        Long notificacaoId = registrarOuAtualizarPendente(ordem, dataEnvio);
        agendador.agendar(ordemId, dataEnvio, () -> enviarAgendado(ordem, notificacaoId));

        return ResultadoNotificacao.agendada(dataEnvio, "Lembrete de revisão preventiva agendado.");
    }

    private ResultadoNotificacao enviarAgora(OrdemParaNotificar ordem, LocalDateTime agora) {
        String mensagem = mensagem(ordem);
        boolean enviada = enviar(ordem, mensagem);

        if (ordem.clienteId() != null) {
            gateway.registrar(new NovaNotificacao(
                    ordem.clienteId(),
                    ordem.veiculoId(),
                    ordem.id(),
                    TipoNotificacao.revisao_preventiva,
                    MensagemNotificacao.ASSUNTO_REVISAO,
                    mensagem,
                    enviada ? StatusNotificacao.enviada : StatusNotificacao.erro,
                    ordem.telefoneCliente(),
                    null,
                    enviada ? agora : null
            ));
        }

        if (!enviada) {
            return ResultadoNotificacao.naoEnviada("Lembrete de revisão não registrado: cliente sem telefone cadastrado.");
        }

        if (ordem.id() != null) {
            agendador.marcarProcessado(ordem.id());
        }

        return ResultadoNotificacao.enviadaImediatamente(
                agora, "Data da revisão com menos de " + DIAS_ANTECEDENCIA + " dias; lembrete registrado imediatamente.");
    }

    /** Executado pelo agendador na data prevista. */
    private void enviarAgendado(OrdemParaNotificar ordem, Long notificacaoId) {
        boolean enviada = enviar(ordem, mensagem(ordem));

        if (notificacaoId != null) {
            gateway.registrarResultadoEnvio(notificacaoId, enviada, enviada ? LocalDateTime.now(clock) : null);
        }

        if (enviada && ordem.id() != null) {
            agendador.marcarProcessado(ordem.id());
        }
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

    private boolean enviar(OrdemParaNotificar ordem, String mensagem) {
        String telefone = Telefone.normalizar(ordem.telefoneCliente());
        return telefone != null
                && enviador.enviar(telefone, mensagem, "lembrete de revisão da OS " + ordem.id());
    }

    private String mensagem(OrdemParaNotificar ordem) {
        return MensagemNotificacao.lembreteRevisao(
                ordem.nomeCliente(), ordem.modeloVeiculo(), ordem.placaVeiculo(), ordem.dataProximaRevisao());
    }
}
