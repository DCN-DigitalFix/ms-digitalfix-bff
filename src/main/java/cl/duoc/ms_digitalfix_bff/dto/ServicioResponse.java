package cl.duoc.ms_digitalfix_bff.dto;

/** Espeja ServicioResponse de ms-digitalfix-catalog. */
public record ServicioResponse(
        Long id,
        String nombre,
        String descripcion,
        Double tarifa,
        boolean activo) {
}
