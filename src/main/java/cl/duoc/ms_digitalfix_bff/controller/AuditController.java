package cl.duoc.ms_digitalfix_bff.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_digitalfix_bff.dto.AuditEventView;
import cl.duoc.ms_digitalfix_bff.service.AuditService;

/**
 * Reenvia a ms-digitalfix-audit, que es de solo lectura: no hay POST ni
 * PATCH aqui, igual que alla.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService service;

    @Autowired
    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('Admin', 'Auditor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<List<AuditEventView>> listarUltimos(
            @RequestParam(name = "limite", defaultValue = "50") int limite) {
        return ResponseEntity.ok(service.listarUltimos(limite));
    }

    @GetMapping("/orders/{ordenId}/timeline")
    @PreAuthorize("hasAnyRole('Admin', 'Auditor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<List<AuditEventView>> timeline(@PathVariable String ordenId) {
        return ResponseEntity.ok(service.timelineDeOrden(ordenId));
    }
}
