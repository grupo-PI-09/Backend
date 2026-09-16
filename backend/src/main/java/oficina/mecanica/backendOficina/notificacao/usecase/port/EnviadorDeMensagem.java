package oficina.mecanica.backendOficina.notificacao.usecase.port;

/**
 * Port de saída para o gateway de mensagens (WhatsApp/SMS/log).
 * Trocar o provedor significa apenas trocar a implementação deste contrato.
 */
public interface EnviadorDeMensagem {

    /**
     * @param telefone destinatário já normalizado (somente dígitos)
     * @param mensagem texto a enviar
     * @param contexto descrição curta para rastreabilidade (ex.: "finalização da OS 12")
     * @return {@code true} se o envio foi aceito pelo gateway
     */
    boolean enviar(String telefone, String mensagem, String contexto);
}
