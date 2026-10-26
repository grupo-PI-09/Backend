package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.DTO.NotificacaoEnvioMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import org.springframework.stereotype.Component;

/**
 * Adapter do envio de mensagens: entrega o pedido ao microsserviço de
 * notificação pela fila do RabbitMQ. O resultado do envio volta depois,
 * pela fila de resultado.
 */
@Component
public class RabbitEnviadorDeMensagem implements EnviadorDeMensagem {

    private final NotificacaoEnvioPublisher publisher;

    public RabbitEnviadorDeMensagem(NotificacaoEnvioPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public boolean enviar(Long notificacaoId, String telefone, String mensagem, String mensagemCurta, String contexto) {
        return publisher.publicar(new NotificacaoEnvioMensagem(notificacaoId, telefone, mensagem, mensagemCurta, contexto));
    }
}