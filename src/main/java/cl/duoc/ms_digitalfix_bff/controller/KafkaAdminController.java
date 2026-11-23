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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_digitalfix_bff.dto.ActualizarConfigRequest;
import cl.duoc.ms_digitalfix_bff.dto.AmpliarParticionesRequest;
import cl.duoc.ms_digitalfix_bff.dto.ClusterResponse;
import cl.duoc.ms_digitalfix_bff.dto.ConsumerGroupResponse;
import cl.duoc.ms_digitalfix_bff.dto.ConsumerGroupResumenResponse;
import cl.duoc.ms_digitalfix_bff.dto.CrearTopicoRequest;
import cl.duoc.ms_digitalfix_bff.dto.OperacionResponse;
import cl.duoc.ms_digitalfix_bff.dto.TopicoResponse;
import cl.duoc.ms_digitalfix_bff.service.KafkaAdminProxyService;

import jakarta.validation.Valid;

/**
 * Reenvia a ms-digitalfix-kafka-admin. Todo exige rol Admin, igual que alla:
 * administrar la plataforma de streaming es la operacion mas sensible del
 * sistema.
 */
@RestController
@RequestMapping("/api/v1/kafka")
public class KafkaAdminController {

    @Autowired
    private KafkaAdminProxyService service;

    // --- Topicos -------------------------------------------------------

    @GetMapping("/topics")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<List<TopicoResponse>> listarTopicos() {
        return ResponseEntity.ok(service.listarTopicos());
    }

    @GetMapping("/topics/{nombre}")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<TopicoResponse> obtenerTopico(@PathVariable String nombre) {
        return ResponseEntity.ok(service.obtenerTopico(nombre));
    }

    @PostMapping("/topics")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<TopicoResponse> crearTopico(@Valid @RequestBody CrearTopicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearTopico(request));
    }

    @PutMapping("/topics/{nombre}/config")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<OperacionResponse> actualizarConfiguracion(
            @PathVariable String nombre, @Valid @RequestBody ActualizarConfigRequest request) {
        return ResponseEntity.ok(service.actualizarConfiguracion(nombre, request));
    }

    @PutMapping("/topics/{nombre}/partitions")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<OperacionResponse> ampliarParticiones(
            @PathVariable String nombre, @Valid @RequestBody AmpliarParticionesRequest request) {
        return ResponseEntity.ok(service.ampliarParticiones(nombre, request));
    }

    @DeleteMapping("/topics/{nombre}")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Create')")
    public ResponseEntity<OperacionResponse> eliminarTopico(@PathVariable String nombre) {
        return ResponseEntity.ok(service.eliminarTopico(nombre));
    }

    // --- Consumer groups -----------------------------------------------

    @GetMapping("/consumer-groups")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<List<ConsumerGroupResumenResponse>> listarConsumerGroups() {
        return ResponseEntity.ok(service.listarConsumerGroups());
    }

    @GetMapping("/consumer-groups/{id}")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<ConsumerGroupResponse> obtenerConsumerGroup(@PathVariable String id) {
        return ResponseEntity.ok(service.obtenerConsumerGroup(id));
    }

    // --- Cluster ---------------------------------------------------------

    @GetMapping("/cluster")
    @PreAuthorize("hasRole('Admin') and hasAuthority('SCOPE_OT.Read')")
    public ResponseEntity<ClusterResponse> obtenerCluster() {
        return ResponseEntity.ok(service.obtenerCluster());
    }
}
