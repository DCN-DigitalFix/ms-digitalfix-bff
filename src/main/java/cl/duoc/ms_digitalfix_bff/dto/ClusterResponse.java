package cl.duoc.ms_digitalfix_bff.dto;

import java.util.List;

/** Espeja ClusterResponse de ms-digitalfix-kafka-admin. */
public record ClusterResponse(
        String clusterId,
        BrokerResponse controlador,
        List<BrokerResponse> brokers) {

    public record BrokerResponse(int id, String host, int puerto) {}
}
