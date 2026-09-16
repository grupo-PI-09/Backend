package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.notificacao.usecase.port.AgendadorDeLembrete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Adapter do {@link TaskScheduler} do Spring. Mantém em memória quais OS já
 * têm lembrete agendado ou processado nesta execução da aplicação.
 */
@Component
public class TaskSchedulerAgendadorDeLembrete implements AgendadorDeLembrete {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerAgendadorDeLembrete.class);

    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> agendados = new ConcurrentHashMap<>();
    private final Set<Long> processados = ConcurrentHashMap.newKeySet();

    public TaskSchedulerAgendadorDeLembrete(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public boolean estaAgendado(Long ordemServicoId) {
        return ordemServicoId != null && agendados.containsKey(ordemServicoId);
    }

    @Override
    public boolean foiProcessado(Long ordemServicoId) {
        return ordemServicoId != null && processados.contains(ordemServicoId);
    }

    @Override
    public void marcarProcessado(Long ordemServicoId) {
        if (ordemServicoId != null) {
            processados.add(ordemServicoId);
        }
    }

    @Override
    public void agendar(Long ordemServicoId, LocalDateTime quando, Runnable tarefa) {
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> {
                    try {
                        tarefa.run();
                    } finally {
                        if (ordemServicoId != null) {
                            agendados.remove(ordemServicoId);
                        }
                    }
                },
                quando.atZone(ZoneId.systemDefault()).toInstant()
        );

        if (ordemServicoId != null) {
            agendados.put(ordemServicoId, future);
        }

        log.info("Lembrete de revisão preventiva agendado para OS {} em {}", ordemServicoId, quando);
    }
}
