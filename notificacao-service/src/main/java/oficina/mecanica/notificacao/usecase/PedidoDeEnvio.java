package oficina.mecanica.notificacao.usecase;

/**
 * Dados de entrada do envio, desacoplados do formato da mensagem da fila.
 *
 * @param mensagem      texto completo (WhatsApp)
 * @param mensagemCurta versão para SMS; {@code null} quando a API não mandou
 */
public record PedidoDeEnvio(
        Long notificacaoId,
        String telefone,
        String mensagem,
        String mensagemCurta,
        String contexto
) {
}
