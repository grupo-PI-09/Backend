package oficina.mecanica.notificacao.infra.rabbit;

/**
 * Resultado devolvido à API principal pela fila {@code notificacoes.resultado}.
 * Os campos precisam ter os mesmos nomes do record de mesmo nome na API.
 */
public record NotificacaoResultadoMensagem(
        Long notificacaoId,
        boolean enviada,
        String erro
) {
}
