package oficina.mecanica.notificacao.infra.twilio;

import java.util.Map;

/**
 * Tradução dos códigos de erro mais comuns da Twilio em contas trial e no
 * sandbox do WhatsApp, com a ação para resolver. O texto vai para o log e
 * para o campo {@code erro} do resultado devolvido à API.
 *
 * Referência: https://www.twilio.com/docs/api/errors
 */
final class ErrosTwilio {

    private static final Map<Integer, String> DESCRICOES = Map.ofEntries(
            Map.entry(20003, "Credenciais inválidas: confira TWILIO_ACCOUNT_SID e TWILIO_AUTH_TOKEN."),
            Map.entry(21211, "Número de destino inválido."),
            Map.entry(21212, "Número de origem inválido: confira TWILIO_REMETENTE."),
            Map.entry(21408, "Envio para este país não habilitado: Console > Messaging > Settings > Geo permissions > Brasil."),
            Map.entry(21608, "Conta trial só envia para números verificados: Console > Phone Numbers > Verified Caller IDs."),
            Map.entry(21610, "O destinatário bloqueou mensagens (respondeu STOP)."),
            Map.entry(21659, "O remetente não pertence a esta conta Twilio."),
            Map.entry(30003, "Destino inalcançável (aparelho desligado ou sem sinal)."),
            Map.entry(30005, "Número de destino desconhecido ou inexistente."),
            Map.entry(30006, "Destino é fixo ou não aceita mensagens."),
            Map.entry(30007, "Mensagem filtrada pela operadora."),
            Map.entry(63007, "Remetente não é um canal WhatsApp: no sandbox use TWILIO_REMETENTE=+14155238886."),
            Map.entry(63015, "Destinatário não entrou no sandbox: envie \"join <código>\" no WhatsApp para +1 415 523 8886."),
            Map.entry(63016, "Fora da janela de 24h do WhatsApp: o destinatário precisa mandar qualquer mensagem para o sandbox antes."),
            Map.entry(63018, "Limite de taxa do WhatsApp excedido (sandbox: 1 mensagem a cada 3 segundos)."),
            Map.entry(63038, "Limite diário de mensagens da conta excedido.")
    );

    private ErrosTwilio() {
    }

    static String descrever(Integer codigo, String mensagemOriginal) {
        String prefixo = "Twilio " + (codigo != null ? codigo : "?") + ": ";
        String descricao = codigo != null ? DESCRICOES.get(codigo) : null;

        if (descricao != null) {
            return prefixo + descricao;
        }

        return prefixo + (mensagemOriginal != null ? mensagemOriginal : "erro sem descrição.");
    }
}
