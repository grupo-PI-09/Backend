package oficina.mecanica.backendOficina.Paginacao;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginacaoOffsetTest {

    private static final Sort ORDEM = Sort.by("id");

    @Test
    void paginaTresComTamanhoDezTemOffsetVinte() {
        Pageable pageable = PaginacaoOffset.criar(3, 10, ORDEM);

        assertEquals(10, pageable.getPageSize());
        assertEquals(20, pageable.getOffset());
        assertEquals(ORDEM, pageable.getSort());
    }

    @Test
    void primeiraPaginaTemOffsetZero() {
        assertEquals(0, PaginacaoOffset.criar(1, 8, ORDEM).getOffset());
    }

    @Test
    void rejeitaPaginaMenorQueUm() {
        assertBadRequest(0, 8);
        assertBadRequest(-1, 8);
    }

    @Test
    void rejeitaTamanhoForaDoLimite() {
        assertBadRequest(1, 0);
        assertBadRequest(1, -5);
        assertBadRequest(1, PaginacaoOffset.TAMANHO_MAXIMO + 1);
    }

    private static void assertBadRequest(int pagina, int tamanho) {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> PaginacaoOffset.criar(pagina, tamanho, ORDEM));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }
}
