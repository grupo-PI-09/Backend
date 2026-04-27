package oficina.mecanica.backendOficina.exceptions;

import org.springframework.http.HttpStatus;

public class ApiBrasilException extends RuntimeException {

    private final HttpStatus status;
    private final String detalhes;

    public ApiBrasilException(HttpStatus status, String mensagem, String detalhes) {
        super(mensagem);
        this.status = status;
        this.detalhes = detalhes;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDetalhes() {
        return detalhes;
    }
}
