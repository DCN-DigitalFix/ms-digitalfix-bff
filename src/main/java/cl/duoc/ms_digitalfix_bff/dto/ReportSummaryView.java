package cl.duoc.ms_digitalfix_bff.dto;

import java.util.Map;

/** Espeja ReportSummaryView de ms-digitalfix-report. */
public record ReportSummaryView(
        Map<String, Long> ordenesPorEstado,
        long total,
        Double tiempoPromedioCierreHoras,
        long totalEventosAuditoria) {
}
