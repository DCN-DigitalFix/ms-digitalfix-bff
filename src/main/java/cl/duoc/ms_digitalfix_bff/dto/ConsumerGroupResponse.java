package cl.duoc.ms_digitalfix_bff.dto;

import java.util.List;

/** Espeja ConsumerGroupResponse de ms-digitalfix-kafka-admin. */
public record ConsumerGroupResponse(
        String groupId,
        String estado,
        List<MiembroResponse> miembros,
        List<OffsetParticionResponse> offsets) {

    public record MiembroResponse(
            String memberId,
            String clientId,
            String host,
            List<String> particionesAsignadas) {
    }

    public record OffsetParticionResponse(
            String topico,
            int particion,
            Long offsetActual,
            long offsetFinal,
            Long lag) {
    }
}
