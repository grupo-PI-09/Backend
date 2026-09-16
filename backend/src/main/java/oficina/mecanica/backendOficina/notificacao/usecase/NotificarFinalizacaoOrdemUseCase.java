package oficina.mecanica.backendOficina.notificacao.usecase;

import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.MensagemNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.Telefone;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Avisa o cliente que o serviço da ordem foi finalizado e registra o histórico.
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
        boolean enviada = telefone != null
                && enviador.enviar(telefone, mensagem, "finalização da OS " + ordem.id());

        if (ordem.clienteId() != null) {
            gateway.registrar(new NovaNotificacao(
                    ordem.clienteId(),
                    ordem.veiculoId(),
                    ordem.id(),
                    TipoNotificacao.pos_servico,
                    MensagemNotificacao.ASSUNTO_FINALIZACAO,
                    mensagem,
                    enviada ? StatusNotificacao.enviada : StatusNotificacao.erro,
                    ordem.telefoneCliente(),
                    null,
                    enviada ? LocalDateTime.now(clock) : null
            ));
        }

        if (enviada) {
            return ResultadoNotificacao.enviada("Notificação de finalização registrada para o cliente.");
        }

        return ResultadoNotificacao.naoEnviada("Notificação de finalização não registrada: cliente sem telefone cadastrado.");
    }
}
