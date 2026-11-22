package cl.duoc.ms_digitalfix_bff.dto;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;

/** Espeja ActualizarConfigRequest de ms-digitalfix-kafka-admin. */
public record ActualizarConfigRequest(
        @NotEmpty(message = "debe indicar al menos una configuracion")
        Map<String, String> configuraciones) {
}
