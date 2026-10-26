package oficina.mecanica.notificacao.usecase;

/**
 * Resultado definitivo de um envio. Falhas temporárias do provedor não viram
 * {@code ResultadoEnvio}: elas sobem como {@link ProvedorIndisponivelException}
 * para que a fila tente de novo.
 *
 * @param idProvedor identificador da mensagem no provedor (SID da Twilio), quando houver
 */
public record ResultadoEnvio(boolean enviada, String idProvedor, String erro) {

    public static ResultadoEnvio sucesso(String idProvedor) {
        return new ResultadoEnvio(true, idProvedor, null);
    }

    public static ResultadoEnvio falha(String erro) {
        return new ResultadoEnvio(false, null, erro);
    }

    public static ResultadoEnvio falha(String idProvedor, String erro) {
        return new ResultadoEnvio(false, idProvedor, erro);
    }
}
