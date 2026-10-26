package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.Config.RabbitMQConfig;
import oficina.mecanica.backendOficina.DTO.NotificacaoResultadoMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class NotificacaoResultadoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoResultadoListener.class);

    private final NotificacaoGateway gateway;
    private final Clock clock;

    public NotificacaoResultadoListener(NotificacaoGateway gateway, Clock clock) {
        this.gateway = gateway;
        this.clock = clock;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_RESULTADO)
    public void receber(NotificacaoResultadoMensagem resultado) {
        if (resultado.notificacaoId() == null) {
            log.warn("Resultado de envio recebido sem notificacaoId; ignorado.");
            return;
        }

        if (resultado.enviada()) {
            log.info("Notificação {} enviada com sucesso.", resultado.notificacaoId());
        } else {
            log.warn("Notificação {} não enviada. Motivo: {}", resultado.notificacaoId(), resultado.erro());
        }

        gateway.registrarResultadoEnvio(
                resultado.notificacaoId(),
                resultado.enviada(),
                resultado.enviada() ? LocalDateTime.now(clock) : null
        );
    }
}