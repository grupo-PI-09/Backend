package oficina.mecanica.backendOficina.notificacao.usecase.port;

import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Port de saída para o histórico de notificações.
 */
public interface NotificacaoGateway {

    /** Registra a notificação e devolve o id gerado, ou vazio se não foi possível persistir. */
    Optional<Long> registrar(NovaNotificacao notificacao);

    /**
     * Atualiza mensagem e data de um lembrete de revisão ainda pendente para a OS.
     * Devolve o id do lembrete atualizado, ou vazio quando não existe pendente.
     */
    Optional<Long> atualizarLembreteRevisaoPendente(Long ordemServicoId, String mensagem, LocalDateTime dataAgendamento);

    /** Marca uma notificação agendada como enviada (com data) ou com erro. */
    void registrarResultadoEnvio(Long notificacaoId, boolean enviada, LocalDateTime dataEnvio);
}
