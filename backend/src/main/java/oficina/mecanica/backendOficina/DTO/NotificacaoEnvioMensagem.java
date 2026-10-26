package oficina.mecanica.backendOficina.DTO;

public record NotificacaoEnvioMensagem(
        Long notificacaoId,
        String telefone,
        String mensagem,
        String mensagemCurta,
        String contexto
) {
}
