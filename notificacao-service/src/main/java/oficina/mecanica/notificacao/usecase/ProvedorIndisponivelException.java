package oficina.mecanica.notificacao.usecase;

/**
 * Falha temporária do provedor (rede, timeout, erro 5xx, limite de taxa).
 * Vale a pena tentar de novo; a fila cuida das novas tentativas.
 */
public class ProvedorIndisponivelException extends RuntimeException {

    public ProvedorIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
