package cl.duoc.ms_digitalfix_bff.dto;

import java.time.Instant;

/** Espeja CertificateView de ms-digitalfix-report. */
public record CertificateView(
        String ordenId,
        String cliente,
        String tecnicoAsignado,
        Instant creadaEn,
        Instant cerradaEn,
        Double duracionHoras) {
}
