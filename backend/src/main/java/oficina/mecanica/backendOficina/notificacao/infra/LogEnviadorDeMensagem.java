package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação padrão enquanto não há gateway de mensagens contratado:
 * apenas registra a mensagem no log. Para ativar WhatsApp/SMS, basta
 * substituir este bean por outra implementação de {@link EnviadorDeMensagem}.
 */
@Component
public class LogEnviadorDeMensagem implements EnviadorDeMensagem {

    private static final Logger log = LoggerFactory.getLogger(LogEnviadorDeMensagem.class);

    @Override
    public boolean enviar(String telefone, String mensagem, String contexto) {
        log.info("Notificação de {} registrada (envio externo desativado). Para: {} | Texto: {}",
                contexto, telefone, mensagem);
        return true;
    }
}
