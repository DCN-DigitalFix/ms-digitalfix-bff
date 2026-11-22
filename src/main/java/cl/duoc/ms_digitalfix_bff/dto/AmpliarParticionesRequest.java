package cl.duoc.ms_digitalfix_bff.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Espeja AmpliarParticionesRequest de ms-digitalfix-kafka-admin: nuevo total, no cuantas agregar. */
public record AmpliarParticionesRequest(
        @NotNull(message = "las particiones son obligatorias")
        @Min(value = 1, message = "debe tener al menos 1 particion")
        Integer particiones) {
}
