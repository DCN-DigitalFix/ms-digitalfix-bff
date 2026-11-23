package cl.duoc.ms_digitalfix_bff.dto;

import java.time.LocalDateTime;

/** Orden de trabajo tal como la expone el BFF hacia el frontend. */
public record WorkOrder(
        String id,
        String cliente,
        String descripcion,
        String direccion,
        EstadoOT estado,
        String tecnicoAsignado,
        String creadaPor,
        LocalDateTime creadaEn,
        LocalDateTime actualizadaEn,
        String codigoRepuesto,
        Integer cantidadRepuesto) {
}