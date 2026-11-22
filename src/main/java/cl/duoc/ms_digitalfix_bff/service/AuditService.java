package cl.duoc.ms_digitalfix_bff.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import cl.duoc.ms_digitalfix_bff.dto.AuditEventView;
import cl.duoc.ms_digitalfix_bff.exception.NotFoundException;

/**
 * Cliente hacia ms-digitalfix-audit, mismo patron que WorkOrderService hacia
 * ms-digitalfix-workorders: el RestTemplate (ver RestTemplateConfig) propaga
 * el Authorization del usuario, el BFF no agrega identidad propia.
 */
@Service
public class AuditService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public AuditService(RestTemplate restTemplate,
                         @Value("${digitalfix.servicios.audit}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<AuditEventView> listarUltimos(int limite) {
        ResponseEntity<List<AuditEventView>> response = restTemplate.exchange(
                baseUrl + "/api/audit/events?limite=" + limite,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AuditEventView>>() {});
        return response.getBody();
    }

    /** audit responde 404 cuando la orden no tiene ningun evento registrado. */
    public List<AuditEventView> timelineDeOrden(String ordenId) {
        try {
            ResponseEntity<List<AuditEventView>> response = restTemplate.exchange(
                    baseUrl + "/api/audit/orders/" + ordenId + "/timeline",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AuditEventView>>() {});
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("No hay eventos de auditoria para la orden " + ordenId);
            }
            throw e;
        }
    }
}
