package oficina.mecanica.backendOficina.Config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    private static final Logger log = LoggerFactory.getLogger(TwilioConfig.class);
    private static final String SID_PLACEHOLDER = "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String TOKEN_PLACEHOLDER = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @PostConstruct
    public void init() {
        if (!credenciaisConfiguradas()) {
            log.info("Twilio não inicializado: credenciais não configuradas. Mensagens ficarão em modo mock/log.");
            return;
        }

        Twilio.init(accountSid, authToken);
    }

    private boolean credenciaisConfiguradas() {
        return accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && !SID_PLACEHOLDER.equals(accountSid)
                && !TOKEN_PLACEHOLDER.equals(authToken);
    }
}
