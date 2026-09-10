package oficina.mecanica.backendOficina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class
    BackendOficinaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendOficinaApplication.class, args);
    }
}
