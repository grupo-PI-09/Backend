package oficina.mecanica.notificacao.infra.rabbit;

import oficina.mecanica.notificacao.usecase.port.PublicadorDeResultado;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica o resultado do envio na fila {@code notificacoes.resultado},
 * consumida pelo {@code NotificacaoResultadoListener} da API.
 *
 * Exceções aqui não são engolidas de propósito: se o resultado não puder ser
 * publicado, o pedido volta para nova tentativa em vez de a API ficar com a
 * notificação "pendente" para sempre.
 */
@Component
public class RabbitPublicadorDeResultado implements PublicadorDeResultado {

    private final RabbitTemplate rabbitTemplate;

    public RabbitPublicadorDeResultado(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(Long notificacaoId, boolean enviada, String erro) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_RESULTADO,
                new NotificacaoResultadoMensagem(notificacaoId, enviada, erro),
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );
    }
}
