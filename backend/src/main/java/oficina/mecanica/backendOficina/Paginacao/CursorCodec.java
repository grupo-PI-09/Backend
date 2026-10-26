package oficina.mecanica.backendOficina.Paginacao;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * Cursor composto (data de referência + id) serializado como
 * {@code data=<ISO>&id=<n>} em Base64 URL-safe.
 *
 * O cursor vem do cliente, então é tratado como entrada não confiável: só é
 * aceito se decodificar exatamente neste formato; qualquer outra coisa vira 400.
 * Ele nunca é concatenado em SQL (vai como parâmetro da consulta).
 */
public final class CursorCodec {

    private CursorCodec() {
    }

    public record Cursor(LocalDateTime data, Long id) {
    }

    public static String codificar(LocalDateTime data, Long id) {
        String bruto = "data=" + data + "&id=" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bruto.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decodificar(String cursor) {
        try {
            String bruto = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] partes = bruto.split("&");

            if (partes.length != 2 || !partes[0].startsWith("data=") || !partes[1].startsWith("id=")) {
                throw cursorInvalido();
            }

            return new Cursor(
                    LocalDateTime.parse(partes[0].substring("data=".length())),
                    Long.parseLong(partes[1].substring("id=".length()))
            );
        } catch (IllegalArgumentException | DateTimeParseException e) {
            // NumberFormatException e erro de Base64 também são IllegalArgumentException
            throw cursorInvalido();
        }
    }

    private static ResponseStatusException cursorInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor de paginação inválido");
    }
}
