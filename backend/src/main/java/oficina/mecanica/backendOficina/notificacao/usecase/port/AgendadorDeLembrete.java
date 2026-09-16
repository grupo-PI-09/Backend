package oficina.mecanica.backendOficina.notificacao.usecase.port;

import java.time.LocalDateTime;

/**
 * Port de saída para agendamento de tarefas futuras e controle de
 * duplicidade dos lembretes dentro da execução atual da aplicação.
 */
public interface AgendadorDeLembrete {

    boolean estaAgendado(Long ordemServicoId);

    boolean foiProcessado(Long ordemServicoId);

    void marcarProcessado(Long ordemServicoId);

    void agendar(Long ordemServicoId, LocalDateTime quando, Runnable tarefa);
}
