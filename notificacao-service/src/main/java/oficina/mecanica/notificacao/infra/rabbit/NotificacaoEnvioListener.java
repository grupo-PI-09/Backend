package oficina.mecanica.notificacao.infra.rabbit;

import oficina.mecanica.notificacao.usecase.EnviarNotificacaoUseCase;
import oficina.mecanica.notificacao.usecase.PedidoDeEnvio;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consome os pedidos de envio publicados pela API principal.
 *
 * Se o caso de uso lançar exceção (provedor fora do ar), o Spring tenta de
 * novo conforme {@code spring.rabbitmq.listener.simple.retry.*}; esgotadas as
 * tentativas, o {@link FalhaDefinitivaRecoverer} avisa a API e manda a
 * mensagem para a DLQ.
 */
@Component
public class NotificacaoEnvioListener {

    private final EnviarNotificacaoUseCase enviarNotificacao;

    public NotificacaoEnvioListener(EnviarNotificacaoUseCase enviarNotificacao) {
        this.enviarNotificacao = enviarNotificacao;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ENVIO)
    public void receber(NotificacaoEnvioMensagem mensagem) {
        enviarNotificacao.executar(new PedidoDeEnvio(
                mensagem.notificacaoId(),
                mensagem.telefone(),
                mensagem.mensagem(),
                mensagem.mensagemCurta(),
                mensagem.contexto()
        ));
    }
}
