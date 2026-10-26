package oficina.mecanica.notificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificacaoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacaoServiceApplication.class, args);
    }
}
