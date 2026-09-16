package oficina.mecanica.backendOficina.notificacao.usecase;

import java.time.LocalDateTime;

/**
 * Saída dos casos de uso de notificação.
 */
public record ResultadoNotificacao(
        boolean enviada,
        boolean agendada,
        boolean enviadaImediatamente,
        LocalDateTime dataAgendamento,
        String mensagem,
        String aviso
) {

    public static ResultadoNotificacao enviada(String mensagem) {
        return new ResultadoNotificacao(true, false, true, null, mensagem, null);
    }

    public static ResultadoNotificacao enviadaImediatamente(LocalDateTime quando, String mensagem) {
        return new ResultadoNotificacao(true, false, true, quando, mensagem, null);
    }

    public static ResultadoNotificacao agendada(LocalDateTime dataAgendamento, String mensagem) {
        return new ResultadoNotificacao(false, true, false, dataAgendamento, mensagem, null);
    }

    public static ResultadoNotificacao naoEnviada(String aviso) {
        return new ResultadoNotificacao(false, false, false, null, null, aviso);
    }
}
