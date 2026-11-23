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

import cl.duoc.ms_digitalfix_bff.dto.CambiarEstadoRequest;
import cl.duoc.ms_digitalfix_bff.dto.CrearWorkOrderRequest;
import cl.duoc.ms_digitalfix_bff.dto.WorkOrder;
import cl.duoc.ms_digitalfix_bff.exception.NotFoundException;

@Service
public class WorkOrderService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public WorkOrderService(RestTemplate restTemplate,
                            @Value("${digitalfix.servicios.workorders}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<WorkOrder> listar() {
        return exchangeList("/api/v1/workorders", HttpMethod.GET, null);
    }

    public List<WorkOrder> listarPorCreador(String creador) {
        return listar();
    }

    public WorkOrder obtener(String id) {
        try {
            ResponseEntity<WorkOrder> response = restTemplate.getForEntity(
                    baseUrl + "/api/v1/workorders/" + id, WorkOrder.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("No existe la orden de trabajo " + id);
            }
            throw e;
        }
    }

    public WorkOrder crear(CrearWorkOrderRequest request, String creador) {
        try {
            ResponseEntity<WorkOrder> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/workorders", request, WorkOrder.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("No existe la orden de trabajo");
            }
            throw e;
        }
    }

    public WorkOrder cambiarEstado(String id, CambiarEstadoRequest request) {
        try {
            ResponseEntity<WorkOrder> response = restTemplate.exchange(
                    baseUrl + "/api/v1/workorders/" + id + "/estado",
                    HttpMethod.PATCH,
                    new HttpEntity<>(request),
                    WorkOrder.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("No existe la orden de trabajo " + id);
            }
            throw e;
        }
    }

    public void eliminar(String id) {
        try {
            restTemplate.delete(baseUrl + "/api/v1/workorders/" + id);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("No existe la orden de trabajo " + id);
            }
            throw e;
        }
    }

    private List<WorkOrder> exchangeList(String path, HttpMethod method, Object body) {
        try {
            ResponseEntity<List<WorkOrder>> response = restTemplate.exchange(
                    baseUrl + path,
                    method,
                    body == null ? null : new HttpEntity<>(body),
                    new ParameterizedTypeReference<List<WorkOrder>>() {});
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Recurso no encontrado");
            }
            throw e;
        }
    }
}
