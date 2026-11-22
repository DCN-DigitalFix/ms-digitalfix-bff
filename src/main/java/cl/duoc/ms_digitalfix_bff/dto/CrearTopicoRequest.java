package cl.duoc.ms_digitalfix_bff.dto;

import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Espeja CrearTopicoRequest de ms-digitalfix-kafka-admin, misma validacion. */
public record CrearTopicoRequest(

        @NotBlank(message = "el nombre es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "el nombre solo admite letras, numeros, punto, guion y guion bajo")
        String nombre,

        @NotNull(message = "las particiones son obligatorias")
        @Min(value = 1, message = "debe tener al menos 1 particion")
        Integer particiones,

        @NotNull(message = "el factor de replicacion es obligatorio")
        @Min(value = 1, message = "debe tener al menos 1 replica")
        Integer factorReplicacion,

        Map<String, String> configuraciones) {
}
