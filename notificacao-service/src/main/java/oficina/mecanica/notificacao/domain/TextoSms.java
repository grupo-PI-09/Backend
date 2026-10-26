package oficina.mecanica.notificacao.domain;

import java.text.Normalizer;

/**
 * Prepara texto para SMS.
 *
 * Basta um caractere fora do alfabeto GSM-7 (ex.: "ç", "ã", "í") para a
 * operadora usar UCS-2, em que cada segmento leva só 70 caracteres em vez de
 * 160 — e cada segmento é cobrado como um SMS. Remover os acentos mantém a
 * mensagem legível e em 1 segmento.
 */
public final class TextoSms {

    public static final int LIMITE_SEGMENTO_UNICO = 160;
    private static final int LIMITE_SEGMENTO_CONCATENADO = 153;

    private TextoSms() {
    }

    /** Remove acentos e troca os caracteres que o GSM-7 não tem ou cobra em dobro. */
    public static String paraGsm7(String texto) {
        if (texto == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        return semAcento
                .replace('“', '"').replace('”', '"')
                .replace('‘', '\'').replace('’', '\'').replace('`', '\'')
                .replace('–', '-').replace('—', '-').replace('~', '-')
                .replace('[', '(').replace(']', ')')
                .replace('{', '(').replace('}', ')')
                .replace('|', '/').replace('\\', '/')
                .replace("^", "")
                .replace("…", "...")
                // qualquer outro caractere fora do ASCII imprimível (emoji etc.)
                .replaceAll("[^\\x20-\\x7E\\n]", "");
    }

    /** Quantos SMS serão cobrados para um texto já em GSM-7. */
    public static int segmentos(String textoGsm7) {
        int tamanho = textoGsm7.length();
        if (tamanho <= LIMITE_SEGMENTO_UNICO) {
            return 1;
        }
        return (tamanho + LIMITE_SEGMENTO_CONCATENADO - 1) / LIMITE_SEGMENTO_CONCATENADO;
    }
}
