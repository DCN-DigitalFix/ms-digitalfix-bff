package cl.duoc.ms_digitalfix_bff.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CambiarEstadoRequest(
                @NotNull(message = "el estado es obligatorio") EstadoOT estado,

                String tecnicoAsignado,

                String codigoRepuesto,
                @Positive(message = "la cantidad debe ser mayor que cero") Integer cantidadRepuesto) {
}