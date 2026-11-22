package cl.duoc.ms_digitalfix_bff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import cl.duoc.ms_digitalfix_bff.dto.CertificateView;
import cl.duoc.ms_digitalfix_bff.dto.ReportSummaryView;
import cl.duoc.ms_digitalfix_bff.exception.NotFoundException;

/**
 * Cliente hacia ms-digitalfix-report, mismo patron que AuditService: el
 * RestTemplate (ver RestTemplateConfig) propaga el Authorization del usuario.
 */
@Service
public class ReportService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public ReportService(RestTemplate restTemplate,
                          @Value("${digitalfix.servicios.report}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ReportSummaryView resumen() {
        return restTemplate.getForObject(baseUrl + "/api/v1/reports/summary", ReportSummaryView.class);
    }

    /** report responde 404 si la orden no existe y 409 si existe pero no esta cerrada. */
    public CertificateView certificado(String ordenId) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/api/v1/reports/orders/" + ordenId + "/certificate", CertificateView.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("No hay datos de reporte para la orden " + ordenId);
            }
            throw e;
        }
    }
}
