package cl.duoc.ms_digitalfix_bff.dto;

/** Espeja RepuestoResponse de ms-digitalfix-catalog. */
public record RepuestoResponse(
        Long id,
        String codigo,
        String nombre,
        Integer stock,
        Double precioUnitario) {
}
