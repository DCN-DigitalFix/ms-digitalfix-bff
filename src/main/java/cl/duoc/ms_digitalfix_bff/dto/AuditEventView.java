package cl.duoc.ms_digitalfix_bff.dto;

import java.time.Instant;

/**
 * Evento de auditoria tal como lo expone ms-digitalfix-audit en
 * GET /api/v1/audit/events y GET /api/v1/audit/orders/{id}/timeline. El BFF solo
 * reenvia la forma real de esa API; no agrega campos que audit no devuelve
 * (por ejemplo, no hay particion ni offset de Kafka en la respuesta).
 */
public record AuditEventView(
        String eventId,
        String eventType,
        String ordenId,
        String estadoAnterior,
        String estadoNuevo,
        String cliente,
        String tecnicoAsignado,
        String actor,
        Instant occurredAt,
        Instant registradoEn) {
}
