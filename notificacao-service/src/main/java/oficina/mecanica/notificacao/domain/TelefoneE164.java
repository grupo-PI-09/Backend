package oficina.mecanica.notificacao.domain;

import java.util.Optional;

/**
 * Converte o telefone recebido da API (somente dígitos, geralmente sem DDI)
 * para o formato E.164 exigido pela Twilio: {@code +5511999998888}.
 *
 * A API guarda telefones brasileiros como DDD + número (10 ou 11 dígitos);
 * sem o "+55" a Twilio rejeita o destinatário.
 */
public final class TelefoneE164 {

    private TelefoneE164() {
    }

    /**
     * @param telefone        telefone em qualquer formatação
     * @param codigoPaisPadrao DDI usado quando o número não traz um (ex.: "55")
     * @return o número em E.164, ou vazio quando não é um telefone válido
     */
    public static Optional<String> converter(String telefone, String codigoPaisPadrao) {
        if (telefone == null) {
            return Optional.empty();
        }

        String digitos = telefone.replaceAll("[^0-9]", "");

        // "011 99999-8888" ou "0055..." → remove os zeros de discagem
        digitos = digitos.replaceFirst("^0+", "");

        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = codigoPaisPadrao + digitos;
        }

        // E.164: no máximo 15 dígitos; menos de 12 não é um celular brasileiro com DDI
        if (digitos.length() < 12 || digitos.length() > 15) {
            return Optional.empty();
        }

        return Optional.of("+" + digitos);
    }
}
