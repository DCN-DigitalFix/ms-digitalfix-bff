package cl.duoc.ms_digitalfix_bff.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_digitalfix_bff.dto.CrearRepuestoRequest;
import cl.duoc.ms_digitalfix_bff.dto.CrearServicioRequest;
import cl.duoc.ms_digitalfix_bff.dto.DescontarStockRequest;
import cl.duoc.ms_digitalfix_bff.dto.RepuestoResponse;
import cl.duoc.ms_digitalfix_bff.dto.ServicioResponse;
import cl.duoc.ms_digitalfix_bff.service.CatalogService;

import jakarta.validation.Valid;

/**
 * Reenvia a ms-digitalfix-catalog. Los roles espejan los @PreAuthorize que
 * catalog aplica en sus propios controladores (RepuestoController,
 * ServicioController): lectura para Admin/Supervisor, alta para Admin,
 * descontar stock para Admin/Supervisor.
 */
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService service;

    @Autowired
    public CatalogController(CatalogService service) {
        this.service = service;
    }

    // --- Servicios -------------------------------------------------------

    @GetMapping("/services")
    @PreAuthorize("hasAnyRole('Admin', 'Supervisor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<List<ServicioResponse>> listarServicios(
            @RequestParam(name = "soloActivos", defaultValue = "false") boolean soloActivos) {
        return ResponseEntity.ok(service.listarServicios(soloActivos));
    }

    @GetMapping("/services/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Supervisor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<ServicioResponse> obtenerServicio(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerServicio(id));
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<ServicioResponse> crearServicio(
            @Valid @RequestBody CrearServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearServicio(request));
    }

    @DeleteMapping("/services/{id}")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<ServicioResponse> desactivarServicio(@PathVariable Long id) {
        return ResponseEntity.ok(service.desactivarServicio(id));
    }

    // --- Repuestos ---------------------------------------------------------

    @GetMapping("/repuestos")
    @PreAuthorize("hasAnyRole('Admin', 'Supervisor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<List<RepuestoResponse>> listarRepuestos() {
        return ResponseEntity.ok(service.listarRepuestos());
    }

    @GetMapping("/repuestos/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Supervisor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<RepuestoResponse> obtenerRepuesto(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerRepuesto(id));
    }

    @PostMapping("/repuestos")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<RepuestoResponse> crearRepuesto(
            @Valid @RequestBody CrearRepuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearRepuesto(request));
    }

    @PostMapping("/repuestos/{codigo}/descontar")
    @PreAuthorize("hasAnyRole('Admin', 'Supervisor') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<RepuestoResponse> descontarStock(
            @PathVariable String codigo, @Valid @RequestBody DescontarStockRequest request) {
        return ResponseEntity.ok(service.descontarStock(codigo, request));
    }
}
