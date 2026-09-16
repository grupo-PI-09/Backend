package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.notificacao.usecase.AgendarLembreteRevisaoUseCase;
import oficina.mecanica.backendOficina.notificacao.usecase.NotificarFinalizacaoOrdemUseCase;
import oficina.mecanica.backendOficina.notificacao.usecase.port.AgendadorDeLembrete;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Monta os casos de uso a partir dos adapters. Os casos de uso não conhecem
 * o Spring: toda a ligação com o framework acontece aqui.
 */
@Configuration
public class NotificacaoUseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public NotificarFinalizacaoOrdemUseCase notificarFinalizacaoOrdemUseCase(EnviadorDeMensagem enviador,
                                                                             NotificacaoGateway gateway,
                                                                             Clock clock) {
        return new NotificarFinalizacaoOrdemUseCase(enviador, gateway, clock);
    }

    @Bean
    public AgendarLembreteRevisaoUseCase agendarLembreteRevisaoUseCase(EnviadorDeMensagem enviador,
                                                                       NotificacaoGateway gateway,
                                                                       AgendadorDeLembrete agendador,
                                                                       Clock clock) {
        return new AgendarLembreteRevisaoUseCase(enviador, gateway, agendador, clock);
    }
}
