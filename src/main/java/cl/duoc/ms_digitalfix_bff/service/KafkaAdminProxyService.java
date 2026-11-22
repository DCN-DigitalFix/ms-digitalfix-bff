package cl.duoc.ms_digitalfix_bff.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import cl.duoc.ms_digitalfix_bff.dto.ActualizarConfigRequest;
import cl.duoc.ms_digitalfix_bff.dto.AmpliarParticionesRequest;
import cl.duoc.ms_digitalfix_bff.dto.ClusterResponse;
import cl.duoc.ms_digitalfix_bff.dto.ConsumerGroupResponse;
import cl.duoc.ms_digitalfix_bff.dto.ConsumerGroupResumenResponse;
import cl.duoc.ms_digitalfix_bff.dto.CrearTopicoRequest;
import cl.duoc.ms_digitalfix_bff.dto.OperacionResponse;
import cl.duoc.ms_digitalfix_bff.dto.TopicoResponse;
import cl.duoc.ms_digitalfix_bff.exception.NotFoundException;

/**
 * Cliente hacia ms-digitalfix-kafka-admin. Mismas rutas que alla
 * (/api/v1/kafka/...): a diferencia de catalog, aqui no hace falta traducir el
 * contrato, solo propagar.
 */
@Service
public class KafkaAdminProxyService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public KafkaAdminProxyService(RestTemplate restTemplate,
                                   @Value("${digitalfix.servicios.kafka-admin}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // --- Topicos -------------------------------------------------------

    public List<TopicoResponse> listarTopicos() {
        ResponseEntity<List<TopicoResponse>> response = restTemplate.exchange(
                baseUrl + "/api/v1/kafka/topics", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<TopicoResponse>>() {});
        return response.getBody();
    }

    public TopicoResponse obtenerTopico(String nombre) {
        try {
            return restTemplate.getForObject(baseUrl + "/api/v1/kafka/topics/" + nombre, TopicoResponse.class);
        } catch (HttpClientErrorException e) {
            throw traducirNotFound(e, "No existe el topico " + nombre);
        }
    }

    public TopicoResponse crearTopico(CrearTopicoRequest request) {
        return restTemplate.postForObject(baseUrl + "/api/v1/kafka/topics", request, TopicoResponse.class);
    }

    public OperacionResponse actualizarConfiguracion(String nombre, ActualizarConfigRequest request) {
        ResponseEntity<OperacionResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/kafka/topics/" + nombre + "/config", HttpMethod.PUT,
                new HttpEntity<>(request), OperacionResponse.class);
        return response.getBody();
    }

    public OperacionResponse ampliarParticiones(String nombre, AmpliarParticionesRequest request) {
        ResponseEntity<OperacionResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/kafka/topics/" + nombre + "/partitions", HttpMethod.PUT,
                new HttpEntity<>(request), OperacionResponse.class);
        return response.getBody();
    }

    public OperacionResponse eliminarTopico(String nombre) {
        try {
            ResponseEntity<OperacionResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v1/kafka/topics/" + nombre, HttpMethod.DELETE,
                    null, OperacionResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw traducirNotFound(e, "No existe el topico " + nombre);
        }
    }

    // --- Consumer groups -----------------------------------------------

    public List<ConsumerGroupResumenResponse> listarConsumerGroups() {
        ResponseEntity<List<ConsumerGroupResumenResponse>> response = restTemplate.exchange(
                baseUrl + "/api/v1/kafka/consumer-groups", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ConsumerGroupResumenResponse>>() {});
        return response.getBody();
    }

    public ConsumerGroupResponse obtenerConsumerGroup(String groupId) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/api/v1/kafka/consumer-groups/" + groupId, ConsumerGroupResponse.class);
        } catch (HttpClientErrorException e) {
            throw traducirNotFound(e, "No existe el consumer group " + groupId);
        }
    }

    // --- Cluster ---------------------------------------------------------

    public ClusterResponse obtenerCluster() {
        return restTemplate.getForObject(baseUrl + "/api/v1/kafka/cluster", ClusterResponse.class);
    }

    // -----------------------------------------------------------------------

    private RuntimeException traducirNotFound(HttpClientErrorException e, String mensajePorDefecto) {
        if (e.getStatusCode().value() == 404) {
            return new NotFoundException(mensajePorDefecto);
        }
        return e;
    }
}
