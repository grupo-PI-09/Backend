package oficina.mecanica.backendOficina.Paginacao;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validação dos parâmetros da paginação por offset. O tamanho máximo evita
 * que um cliente peça "a tabela inteira" numa única página.
 */
public final class PaginacaoOffset {

    public static final int TAMANHO_PADRAO = 8;
    public static final int TAMANHO_MAXIMO = 50;

    private PaginacaoOffset() {
    }

    /**
     * @param pagina  número da página, começando em 1
     * @param tamanho quantidade de registros por página
     * @param ordem   deve terminar com uma coluna única (desempate), senão a
     *                mesma consulta pode devolver ordens diferentes
     */
    public static Pageable criar(int pagina, int tamanho, Sort ordem) {
        if (pagina < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A página deve ser maior ou igual a 1");
        }

        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO);
        }

        return PageRequest.of(pagina - 1, tamanho, ordem);
    }
}
