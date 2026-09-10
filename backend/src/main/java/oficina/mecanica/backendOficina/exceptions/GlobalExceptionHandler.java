package oficina.mecanica.backendOficina.exceptions;

import oficina.mecanica.backendOficina.DTO.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OWASP A05:2021 - o detalhe tecnico da falha (mensagem da excecao, SQL,
 * nomes de tabelas) nunca vai para o cliente: fica no log do servidor.
 * O cliente recebe uma mensagem generica e um identificador de correlacao.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiBrasilException.class)
    public ResponseEntity<ApiErrorResponse> handleApiBrasil(ApiBrasilException ex) {
        // A resposta bruta da API externa fica apenas no log.
        log.warn("Falha na consulta a APIBrasil: {} | detalhes: {}", ex.getMessage(), ex.getDetalhes());
        return ResponseEntity.status(ex.getStatus()).body(
                new ApiErrorResponse(true, ex.getMessage(), null)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Mensagens de validacao sao definidas por nos e nao expoem detalhe interno.
        String detalhes = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(true, "Dados inválidos", detalhes)
        );
    }

    @ExceptionHandler({ClienteNaoEncontradoException.class, VeiculoNaoEncontradoException.class})
    public ResponseEntity<ApiErrorResponse> handleNaoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiErrorResponse(true, ex.getMessage(), null)
        );
    }

    /**
     * Sem este handler, uma negativa de autorizacao vinda de @PreAuthorize
     * cairia no handler generico e seria devolvida como 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiErrorResponse(true, "Acesso negado", null)
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        // Apenas o "reason", definido no proprio codigo. ex.getMessage() traria
        // o status e detalhes internos concatenados.
        return ResponseEntity.status(ex.getStatusCode()).body(
                new ApiErrorResponse(true, ex.getReason(), null)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        String idErro = UUID.randomUUID().toString().substring(0, 8);
        log.error("Erro interno nao tratado [{}]", idErro, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiErrorResponse(true, "Erro interno no servidor",
                        "Código de referência: " + idErro)
        );
    }
}
