package oficina.mecanica.notificacao.infra.rabbit;

/**
 * Pedido de envio publicado pela API principal na fila {@code notificacoes.envio}.
 * Os campos precisam ter os mesmos nomes do record de mesmo nome na API.
 *
 * @param mensagemCurta versão para SMS (até 160 caracteres); pode vir {@code null}
 */
public record NotificacaoEnvioMensagem(
        Long notificacaoId,
        String telefone,
        String mensagem,
        String mensagemCurta,
        String contexto
) {
}
