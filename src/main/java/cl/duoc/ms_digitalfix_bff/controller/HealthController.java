package cl.duoc.ms_digitalfix_bff.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ruta publica. Sirve para demostrar en la defensa el contraste entre un endpoint
 * abierto y uno protegido: esta responde 200 sin token, el resto responde 401.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "servicio", "ms-digitalfix-bff",
                "estado", "UP",
                "timestamp", LocalDateTime.now()));
    }
}