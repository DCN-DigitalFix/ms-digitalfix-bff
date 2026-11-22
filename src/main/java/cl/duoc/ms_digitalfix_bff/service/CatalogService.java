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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cl.duoc.ms_digitalfix_bff.dto.CrearRepuestoRequest;
import cl.duoc.ms_digitalfix_bff.dto.CrearServicioRequest;
import cl.duoc.ms_digitalfix_bff.dto.DescontarStockRequest;
import cl.duoc.ms_digitalfix_bff.dto.RepuestoResponse;
import cl.duoc.ms_digitalfix_bff.dto.ServicioResponse;
import cl.duoc.ms_digitalfix_bff.exception.NotFoundException;
import cl.duoc.ms_digitalfix_bff.exception.StockInsuficienteException;

/**
 * Cliente hacia ms-digitalfix-catalog. Las rutas reales de ese servicio son
 * /api/v1/catalog/services y /api/v1/catalog/repuestos (ver
 * RepuestoController y ServicioController alla); el BFF expone su propio
 * contrato mas simple (/api/catalog/...) hacia el frontend y aqui traduce.
 *
 * El BFF no declara jackson-databind (no lo necesita para nada mas), asi que
 * el campo "message" del cuerpo de error de catalog se extrae con una
 * expresion regular simple en vez de deserializar el JSON completo.
 */
@Service
public class CatalogService {

    private static final Pattern CAMPO_MESSAGE = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public CatalogService(RestTemplate restTemplate,
                           @Value("${digitalfix.servicios.catalog}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // --- Servicios -----------------------------------------------------

    public List<ServicioResponse> listarServicios(boolean soloActivos) {
        ResponseEntity<List<ServicioResponse>> response = restTemplate.exchange(
                baseUrl + "/api/v1/catalog/services?soloActivos=" + soloActivos,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ServicioResponse>>() {});
        return response.getBody();
    }

    public ServicioResponse obtenerServicio(Long id) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/api/v1/catalog/services/" + id, ServicioResponse.class);
        } catch (HttpClientErrorException e) {
            throw traducirNotFound(e, "No existe el servicio " + id);
        }
    }

    public ServicioResponse crearServicio(CrearServicioRequest request) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/catalog/services", request, ServicioResponse.class);
    }

    public ServicioResponse desactivarServicio(Long id) {
        try {
            ResponseEntity<ServicioResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v1/catalog/services/" + id,
                    HttpMethod.DELETE,
                    null,
                    ServicioResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw traducirNotFound(e, "No existe el servicio " + id);
        }
    }

    // --- Repuestos -------------------------------------------------------

    public List<RepuestoResponse> listarRepuestos() {
        ResponseEntity<List<RepuestoResponse>> response = restTemplate.exchange(
                baseUrl + "/api/v1/catalog/repuestos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepuestoResponse>>() {});
        return response.getBody();
    }

    public RepuestoResponse obtenerRepuesto(Long id) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/api/v1/catalog/repuestos/" + id, RepuestoResponse.class);
        } catch (HttpClientErrorException e) {
            throw traducirNotFound(e, "No existe el repuesto " + id);
        }
    }

    public RepuestoResponse crearRepuesto(CrearRepuestoRequest request) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/catalog/repuestos", request, RepuestoResponse.class);
    }

    /** catalog responde 404 si el codigo no existe y 409 si el stock no alcanza. */
    public RepuestoResponse descontarStock(String codigo, DescontarStockRequest request) {
        try {
            ResponseEntity<RepuestoResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v1/catalog/repuestos/" + codigo + "/descontar",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    RepuestoResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("No existe el repuesto con codigo " + codigo);
            }
            if (e.getStatusCode().value() == 409) {
                throw new StockInsuficienteException(mensajeDe(e));
            }
            throw e;
        }
    }

    // -----------------------------------------------------------------------

    private RuntimeException traducirNotFound(HttpClientErrorException e, String mensajePorDefecto) {
        if (e.getStatusCode().value() == 404) {
            return new NotFoundException(mensajePorDefecto);
        }
        return e;
    }

    /** Extrae el campo "message" del ApiError que devuelve catalog; si no hay match, usa el cuerpo crudo. */
    private String mensajeDe(HttpClientErrorException e) {
        String cuerpo = e.getResponseBodyAsString();
        Matcher m = CAMPO_MESSAGE.matcher(cuerpo);
        return m.find() ? m.group(1) : cuerpo;
    }
}
