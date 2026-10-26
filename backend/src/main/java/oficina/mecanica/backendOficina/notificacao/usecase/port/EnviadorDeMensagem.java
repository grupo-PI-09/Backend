package oficina.mecanica.backendOficina.notificacao.usecase.port;

/**
 * Port de saída para o envio de mensagens (WhatsApp/SMS).
 * O envio é assíncrono: o adapter entrega o pedido ao microsserviço de
 * notificação e o resultado real volta depois, por outro caminho.
 */
public interface EnviadorDeMensagem {

    /**
     * @param notificacaoId id do registro no histórico, devolvido junto com o resultado do envio
     *                      (pode ser {@code null} quando não há histórico para atualizar)
     * @param telefone destinatário já normalizado (somente dígitos)
     * @param mensagem texto completo (WhatsApp)
     * @param mensagemCurta versão para SMS, em até 160 caracteres; {@code null} usa a completa
     * @param contexto descrição curta para rastreabilidade (ex.: "finalização da OS 12")
     * @return {@code true} se o pedido de envio foi aceito para processamento;
     *         não significa que a mensagem já foi entregue
     */
    boolean enviar(Long notificacaoId, String telefone, String mensagem, String mensagemCurta, String contexto);
}
