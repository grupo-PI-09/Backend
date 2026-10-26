package oficina.mecanica.notificacao.infra.rabbit;

import oficina.mecanica.notificacao.usecase.EnviarNotificacaoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

/**
 * Chamado pelo Spring quando todas as tentativas de processar um pedido
 * falharam. Avisa a API (para a notificação sair de "pendente" e virar
 * "erro") e rejeita a mensagem sem recolocar na fila, o que a manda para a
 * DLQ {@code notificacoes.envio.dlq} — sem isso o RabbitMQ reentregaria a
 * mensagem em loop infinito.
 */
@Component
public class FalhaDefinitivaRecoverer implements MessageRecoverer {

    private static final Logger log = LoggerFactory.getLogger(FalhaDefinitivaRecoverer.class);

    private final EnviarNotificacaoUseCase enviarNotificacao;
    private final MessageConverter conversor;

    public FalhaDefinitivaRecoverer(EnviarNotificacaoUseCase enviarNotificacao, MessageConverter conversor) {
        this.enviarNotificacao = enviarNotificacao;
        this.conversor = conversor;
    }

    @Override
    public void recover(Message message, Throwable causa) {
        String motivo = causaRaiz(causa).getMessage();
        Long notificacaoId = extrairNotificacaoId(message);

        log.error("Tentativas esgotadas para a notificação {}; mensagem enviada para a DLQ. Motivo: {}",
                notificacaoId, motivo);

        try {
            enviarNotificacao.registrarFalhaDefinitiva(notificacaoId, "Falha após novas tentativas: " + motivo);
        } catch (Exception e) {
            log.error("Não foi possível avisar a API sobre a falha da notificação {}: {}", notificacaoId, e.getMessage());
        }

        throw new AmqpRejectAndDontRequeueException("Tentativas esgotadas", causa);
    }

    private Long extrairNotificacaoId(Message message) {
        try {
            message.getMessageProperties().setInferredArgumentType(NotificacaoEnvioMensagem.class);
            Object corpo = conversor.fromMessage(message);
            return corpo instanceof NotificacaoEnvioMensagem pedido ? pedido.notificacaoId() : null;
        } catch (Exception e) {
            // Mensagem malformada: não há como saber qual notificação era.
            return null;
        }
    }

    private static Throwable causaRaiz(Throwable erro) {
        Throwable atual = erro;
        while (atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual;
    }
}
