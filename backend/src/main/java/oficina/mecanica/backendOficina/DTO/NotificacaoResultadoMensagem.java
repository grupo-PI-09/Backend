package oficina.mecanica.backendOficina.DTO;

public record NotificacaoResultadoMensagem(
        Long notificacaoId,
        boolean enviada,
        String erro
) {
}