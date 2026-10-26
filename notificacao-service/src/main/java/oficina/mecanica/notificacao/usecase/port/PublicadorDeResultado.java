package oficina.mecanica.notificacao.usecase.port;

/**
 * Port de saída que devolve à API principal o resultado do envio,
 * para que ela atualize o status da notificação no histórico.
 */
public interface PublicadorDeResultado {

    void publicar(Long notificacaoId, boolean enviada, String erro);
}
