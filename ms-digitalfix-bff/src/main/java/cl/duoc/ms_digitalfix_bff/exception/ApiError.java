package cl.duoc.ms_digitalfix_bff.exception;

import java.time.LocalDateTime;

/** Cuerpo uniforme de error, para que el frontend no reciba HTML en un 401. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path);
    }

    /**
     * Serializacion manual para los handlers de seguridad, que escriben directo
     * en la respuesta sin pasar por el conversor de mensajes de Spring MVC.
     */
    public static String json(int status, String error, String message, String path) {
        return """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}"""
                .formatted(LocalDateTime.now(), status, error, message, path);
    }
}