package oficina.mecanica.notificacao.infra;

import com.twilio.http.TwilioRestClient;
import oficina.mecanica.notificacao.infra.twilio.TwilioProperties;
import oficina.mecanica.notificacao.infra.twilio.TwilioProvedorDeMensagem;
import oficina.mecanica.notificacao.usecase.EnviarNotificacaoUseCase;
import oficina.mecanica.notificacao.usecase.port.ProvedorDeMensagem;
import oficina.mecanica.notificacao.usecase.port.PublicadorDeResultado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Monta o caso de uso e escolhe o provedor. O caso de uso não conhece o
 * Spring nem a Twilio: toda a ligação acontece aqui.
 */
@Configuration
public class NotificacaoUseCaseConfig {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoUseCaseConfig.class);

    @Bean
    public ProvedorDeMensagem provedorDeMensagem(TwilioProperties twilio) {
        if (!twilio.enabled()) {
            log.warn("Twilio desativada (twilio.enabled=false): as mensagens serão apenas registradas no log.");
            return new LogProvedorDeMensagem();
        }

        // Fail secure: com a Twilio ligada e credencial faltando, o serviço não sobe.
        List<String> faltando = twilio.camposFaltando();
        if (!faltando.isEmpty()) {
            throw new IllegalStateException("twilio.enabled=true, mas faltam: " + String.join(", ", faltando));
        }

        log.info("Twilio ativada: canal {}, remetente {}{}.",
                twilio.canal(),
                twilio.usaMessagingService() ? twilio.messagingServiceSid() : twilio.remetente(),
                twilio.temDestinoTeste() ? ", destino de teste " + twilio.destinoTeste() : "");

        TwilioRestClient client = new TwilioRestClient.Builder(twilio.accountSid(), twilio.authToken()).build();
        return new TwilioProvedorDeMensagem(client, twilio);
    }

    @Bean
    public EnviarNotificacaoUseCase enviarNotificacaoUseCase(ProvedorDeMensagem provedor,
                                                             PublicadorDeResultado publicador,
                                                             @Value("${notificacao.codigo-pais-padrao:55}") String codigoPais) {
        return new EnviarNotificacaoUseCase(provedor, publicador, codigoPais);
    }
}
