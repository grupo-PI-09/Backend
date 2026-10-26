package oficina.mecanica.backendOficina.notificacao.usecase.port;

import java.time.LocalDateTime;

/**
 * Port de saída para agendamento de tarefas futuras e controle de
 * duplicidade dos lembretes dentro da execução atual da aplicação.
 */
public interface AgendadorDeLembrete {

    /** Indica se já existe lembrete agendado para a OS exatamente nesse horário. */
    boolean estaAgendado(Long ordemServicoId, LocalDateTime quando);

    /** Indica se o lembrete da OS para essa data de revisão já foi enviado. */
    boolean foiProcessado(Long ordemServicoId, LocalDateTime dataRevisao);

    void marcarProcessado(Long ordemServicoId, LocalDateTime dataRevisao);

    void agendar(Long ordemServicoId, LocalDateTime quando, Runnable tarefa);

    /** Cancela o lembrete agendado da OS, se houver. */
    void cancelar(Long ordemServicoId);
}
