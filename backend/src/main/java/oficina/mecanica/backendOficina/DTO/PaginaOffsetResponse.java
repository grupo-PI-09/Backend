package oficina.mecanica.backendOficina.DTO;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Resposta da paginação por offset (LIMIT/OFFSET). A página é 1-based para
 * casar com os botões numerados da tela.
 */
public class PaginaOffsetResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public PaginaOffsetResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = page <= 1;
        this.last = page >= totalPages;
    }

    public static <E, T> PaginaOffsetResponse<T> de(Page<E> pagina, Function<E, T> conversor) {
        return new PaginaOffsetResponse<>(
                pagina.getContent().stream().map(conversor).toList(),
                pagina.getNumber() + 1,
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    public static <E, T> PaginaOffsetResponse<T> de(Page<E> pagina, List<T> conteudo) {
        return new PaginaOffsetResponse<>(
                conteudo,
                pagina.getNumber() + 1,
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
