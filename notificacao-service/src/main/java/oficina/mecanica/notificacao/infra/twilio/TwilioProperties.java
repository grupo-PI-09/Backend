package oficina.mecanica.notificacao.infra.twilio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuração da Twilio (prefixo {@code twilio.*}).
 *
 * @param enabled                 {@code false} = apenas registra no log (modo simulado, sem custo)
 * @param accountSid              Account SID (começa com "AC"), no Console da Twilio
 * @param authToken               Auth Token da conta
 * @param canal                   {@code whatsapp} ou {@code sms}
 * @param remetente               número de origem em E.164. No sandbox do WhatsApp: +14155238886
 * @param messagingServiceSid     opcional: usa um Messaging Service (MG...) no lugar do remetente
 * @param destinoTeste            opcional: redireciona TODAS as mensagens para este número.
 *                                Útil na conta trial, que só envia para números verificados.
 * @param confirmacaoTimeout      quanto tempo esperar a Twilio confirmar a entrega. Erros como
 *                                63015/63016 só aparecem depois que a mensagem é aceita.
 *                                {@code 0s} desliga a confirmação.
 */
@ConfigurationProperties(prefix = "twilio")
public record TwilioProperties(
        @DefaultValue("false") boolean enabled,
        String accountSid,
        String authToken,
        @DefaultValue("whatsapp") Canal canal,
        String remetente,
        String messagingServiceSid,
        String destinoTeste,
        @DefaultValue("10s") Duration confirmacaoTimeout
) {

    public enum Canal {
        whatsapp,
        sms
    }

    /** Lista o que falta para usar a Twilio de verdade; vazia quando está tudo certo. */
    public List<String> camposFaltando() {
        List<String> faltando = new ArrayList<>();
        if (vazio(accountSid)) {
            faltando.add("twilio.account-sid (TWILIO_ACCOUNT_SID)");
        }
        if (vazio(authToken)) {
            faltando.add("twilio.auth-token (TWILIO_AUTH_TOKEN)");
        }
        if (vazio(remetente) && vazio(messagingServiceSid)) {
            faltando.add("twilio.remetente (TWILIO_REMETENTE) ou twilio.messaging-service-sid");
        }
        return faltando;
    }

    public boolean temDestinoTeste() {
        return !vazio(destinoTeste);
    }

    public boolean usaMessagingService() {
        return !vazio(messagingServiceSid);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
