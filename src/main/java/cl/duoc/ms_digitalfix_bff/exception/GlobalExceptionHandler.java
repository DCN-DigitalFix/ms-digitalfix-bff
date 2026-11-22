package cl.duoc.ms_digitalfix_bff.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/** Errores de negocio y validacion con el mismo formato que los de seguridad. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.web.client.HttpClientErrorException.class)
    public ResponseEntity<ApiError> handleHttpClientError(
            org.springframework.web.client.HttpClientErrorException ex, WebRequest request) {
        ApiError body = ApiError.of(
                ex.getStatusCode().value(),
                ex.getStatusText(),
                ex.getResponseBodyAsString(),
                path(request));
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, WebRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                path(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ApiError> handleStockInsuficiente(
            StockInsuficienteException ex, WebRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                path(request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Datos invalidos");

        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                detail,
                path(request));

        return ResponseEntity.badRequest().body(body);
    }

    private String path(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}