package cl.duoc.ms_digitalfix_bff.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Espeja DescontarStockRequest de ms-digitalfix-catalog. */
public record DescontarStockRequest(

        @NotNull(message = "la cantidad es obligatoria")
        @Positive(message = "la cantidad debe ser mayor que cero")
        Integer cantidad) {
}
