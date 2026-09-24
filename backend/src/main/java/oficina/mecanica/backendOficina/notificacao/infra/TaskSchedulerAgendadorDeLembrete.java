package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.notificacao.usecase.port.AgendadorDeLembrete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adapter do {@link TaskScheduler} do Spring. Mantém em memória quais OS já
 * têm lembrete agendado ou processado nesta execução da aplicação. Após um
 * restart, o RevisaoScheduler diário reagenda os lembretes pendentes.
 */
@Component
public class TaskSchedulerAgendadorDeLembrete implements AgendadorDeLembrete {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerAgendadorDeLembrete.class);

    private final TaskScheduler taskScheduler;
    private final Map<Long, Agendamento> agendados = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> processados = new ConcurrentHashMap<>();

    public TaskSchedulerAgendadorDeLembrete(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public boolean estaAgendado(Long ordemServicoId, LocalDateTime quando) {
        if (ordemServicoId == null) {
            return false;
        }

        Agendamento agendamento = agendados.get(ordemServicoId);
        return agendamento != null && agendamento.quando().equals(quando);
    }

    @Override
    public boolean foiProcessado(Long ordemServicoId, LocalDateTime dataRevisao) {
        return ordemServicoId != null && dataRevisao != null
                && dataRevisao.equals(processados.get(ordemServicoId));
    }

    @Override
    public void marcarProcessado(Long ordemServicoId, LocalDateTime dataRevisao) {
        if (ordemServicoId != null && dataRevisao != null) {
            processados.put(ordemServicoId, dataRevisao);
        }
    }

    @Override
    public void agendar(Long ordemServicoId, LocalDateTime quando, Runnable tarefa) {
        AtomicReference<Agendamento> referencia = new AtomicReference<>();

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> {
                    try {
                        tarefa.run();
                    } catch (RuntimeException e) {
                        log.error("Falha ao executar lembrete de revisão da OS {}", ordemServicoId, e);
                    } finally {
                        // Remove somente o proprio agendamento, nunca um que o substituiu.
                        Agendamento proprio = referencia.get();
                        if (ordemServicoId != null && proprio != null) {
                            agendados.remove(ordemServicoId, proprio);
                        }
                    }
                },
                quando.atZone(ZoneId.systemDefault()).toInstant()
        );

        Agendamento agendamento = new Agendamento(future, quando);
        referencia.set(agendamento);

        if (ordemServicoId != null) {
            Agendamento anterior = agendados.put(ordemServicoId, agendamento);
            if (anterior != null) {
                anterior.future().cancel(false);
            }
        }

        log.info("Lembrete de revisão preventiva agendado para OS {} em {}", ordemServicoId, quando);
    }

    @Override
    public void cancelar(Long ordemServicoId) {
        if (ordemServicoId == null) {
            return;
        }

        Agendamento agendamento = agendados.remove(ordemServicoId);
        if (agendamento != null) {
            agendamento.future().cancel(false);
            log.info("Lembrete de revisão da OS {} cancelado (estava previsto para {})",
                    ordemServicoId, agendamento.quando());
        }
    }

    private record Agendamento(ScheduledFuture<?> future, LocalDateTime quando) {
    }
}
