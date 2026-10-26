package oficina.mecanica.backendOficina.Paginacao;

import oficina.mecanica.backendOficina.Paginacao.CursorCodec.Cursor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CursorCodecTest {

    @Test
    void codificaEDecodificaMantendoDataEId() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 10, 14, 30, 5);

        Cursor cursor = CursorCodec.decodificar(CursorCodec.codificar(data, 42L));

        assertEquals(data, cursor.data());
        assertEquals(42L, cursor.id());
    }

    @Test
    void cursorEUrlSafeESemPadding() {
        String cursor = CursorCodec.codificar(LocalDateTime.of(2026, 5, 10, 14, 30), 7L);

        assertTrue(cursor.matches("[A-Za-z0-9_-]+"), "cursor deve poder ir na query string sem escape: " + cursor);
    }

    @Test
    void rejeitaTextoQueNaoEBase64() {
        assertCursorInvalido("%%%não-é-base64%%%");
    }

    @Test
    void rejeitaFormatoDiferenteDoEsperado() {
        assertCursorInvalido(base64("(id=10)"));
        assertCursorInvalido(base64("data=2026-05-10T10:00&id=1&extra=2"));
        assertCursorInvalido(base64("id=1&data=2026-05-10T10:00"));
    }

    @Test
    void rejeitaDataEIdMalformados() {
        assertCursorInvalido(base64("data=ontem&id=1"));
        assertCursorInvalido(base64("data=2026-05-10T10:00&id=abc"));
        assertCursorInvalido(base64("data=2026-05-10T10:00&id=1 OR 1=1"));
    }

    private static void assertCursorInvalido(String cursor) {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () -> CursorCodec.decodificar(cursor));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    private static String base64(String texto) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }
}
