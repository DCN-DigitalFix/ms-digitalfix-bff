package cl.duoc.ms_digitalfix_bff.dto;

import java.util.Map;

/** Espeja TopicoResponse de ms-digitalfix-kafka-admin. */
public record TopicoResponse(
        String nombre,
        int particiones,
        int factorReplicacion,
        Map<String, String> configuracion) {
}
