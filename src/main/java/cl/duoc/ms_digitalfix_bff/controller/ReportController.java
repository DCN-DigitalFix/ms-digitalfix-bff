package cl.duoc.ms_digitalfix_bff.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_digitalfix_bff.dto.CertificateView;
import cl.duoc.ms_digitalfix_bff.dto.ReportSummaryView;
import cl.duoc.ms_digitalfix_bff.service.ReportService;

/** Reenvia a ms-digitalfix-report, que es de solo lectura: mismos roles que alla. */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService service;

    @Autowired
    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('Admin', 'Supervisor', 'Auditor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<ReportSummaryView> resumen() {
        return ResponseEntity.ok(service.resumen());
    }

    @GetMapping("/orders/{ordenId}/certificate")
    @PreAuthorize("hasAnyRole('Admin', 'Supervisor', 'Auditor') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<CertificateView> certificado(@PathVariable String ordenId) {
        return ResponseEntity.ok(service.certificado(ordenId));
    }
}
