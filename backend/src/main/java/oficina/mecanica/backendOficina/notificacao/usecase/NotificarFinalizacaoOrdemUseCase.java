package oficina.mecanica.backendOficina.notificacao.usecase;

import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.MensagemNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.Telefone;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;

import java.time.Clock;

/**
 * Avisa o cliente que o serviço da ordem foi finalizado e registra o histórico.
 * A notificação nasce como pendente e só vira enviada ou com erro quando o
 * resultado do envio chega do microsserviço de notificação.
 */
public class NotificarFinalizacaoOrdemUseCase {

    private final EnviadorDeMensagem enviador;
    private final NotificacaoGateway gateway;
    private final Clock clock;

    public NotificarFinalizacaoOrdemUseCase(EnviadorDeMensagem enviador, NotificacaoGateway gateway, Clock clock) {
        this.enviador = enviador;
        this.gateway = gateway;
        this.clock = clock;
    }

    public ResultadoNotificacao executar(OrdemParaNotificar ordem) {
        String mensagem = MensagemNotificacao.finalizacao(
                ordem.nomeCliente(), ordem.modeloVeiculo(), ordem.placaVeiculo());

        String telefone = Telefone.normalizar(ordem.telefoneCliente());

        if (telefone == null) {
            registrar(ordem, mensagem, StatusNotificacao.erro);
            return ResultadoNotificacao.naoEnviada("Notificação de finalização não registrada: cliente sem telefone cadastrado.");
        }

        Long notificacaoId = registrar(ordem, mensagem, StatusNotificacao.pendente);
        String mensagemCurta = MensagemNotificacao.finalizacaoCurta(
                ordem.nomeCliente(), ordem.modeloVeiculo(), ordem.placaVeiculo());
        boolean aceita = enviador.enviar(notificacaoId, telefone, mensagem, mensagemCurta, "finalização da OS " + ordem.id());

        if (!aceita) {
            if (notificacaoId != null) {
                gateway.registrarResultadoEnvio(notificacaoId, false, null);
            }
            return ResultadoNotificacao.naoEnviada("Notificação de finalização não enviada: serviço de mensagens indisponível.");
        }

        return ResultadoNotificacao.enviada("Notificação de finalização registrada para o cliente.");
    }

    private Long registrar(OrdemParaNotificar ordem, String mensagem, StatusNotificacao status) {
        if (ordem.clienteId() == null) {
            return null;
        }

        return gateway.registrar(new NovaNotificacao(
                ordem.clienteId(),
                ordem.veiculoId(),
                ordem.id(),
                TipoNotificacao.pos_servico,
                MensagemNotificacao.ASSUNTO_FINALIZACAO,
                mensagem,
                status,
                ordem.telefoneCliente(),
                null,
                null
        )).orElse(null);
    }
}