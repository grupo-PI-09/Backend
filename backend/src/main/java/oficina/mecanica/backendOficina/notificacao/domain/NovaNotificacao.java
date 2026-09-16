package oficina.mecanica.backendOficina.notificacao.domain;

import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;

import java.time.LocalDateTime;

/**
 * Notificação a ser registrada no histórico. É o que o caso de uso entrega
 * ao gateway de persistência — sem entidade JPA, apenas identificadores.
 */
public record NovaNotificacao(
        Long clienteId,
        Long veiculoId,
        Long ordemServicoId,
        TipoNotificacao tipo,
        String assunto,
        String mensagem,
        StatusNotificacao status,
        String telefoneDestino,
        LocalDateTime dataAgendamento,
        LocalDateTime dataEnvio
) {
}
