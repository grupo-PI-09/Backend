package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.Config.RabbitMQConfig;
import oficina.mecanica.backendOficina.DTO.NotificacaoEnvioMensagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoEnvioPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoEnvioPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificacaoEnvioPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean publicar(NotificacaoEnvioMensagem mensagem) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_ENVIO,
                    mensagem,
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    }
            );
            log.info("Notificação {} publicada na fila de envio. Contexto: {}",
                    mensagem.notificacaoId(), mensagem.contexto());
            return true;
        } catch (Exception e) {
            log.error("Falha ao publicar notificação {} na fila de envio. Contexto: {}",
                    mensagem.notificacaoId(), mensagem.contexto(), e);
            return false;
        }
    }
}