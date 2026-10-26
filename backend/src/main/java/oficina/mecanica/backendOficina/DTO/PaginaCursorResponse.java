package oficina.mecanica.backendOficina.DTO;

import java.util.List;

/**
 * Resposta da paginação por cursor (keyset). Não há total nem número de
 * página: {@code nextPage} é o cursor opaco (Base64) a enviar na próxima
 * chamada e vem {@code null} quando não há mais registros.
 */
public class PaginaCursorResponse<T> {

    private final List<T> content;
    private final String nextPage;
    private final int size;

    public PaginaCursorResponse(List<T> content, String nextPage, int size) {
        this.content = content;
        this.nextPage = nextPage;
        this.size = size;
    }

    public List<T> getContent() { return content; }
    public String getNextPage() { return nextPage; }
    public int getSize() { return size; }
    public boolean isHasNext() { return nextPage != null; }
}
