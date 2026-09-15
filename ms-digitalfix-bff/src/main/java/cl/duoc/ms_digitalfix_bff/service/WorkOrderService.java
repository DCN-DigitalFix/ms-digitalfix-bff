package cl.duoc.ms_digitalfix_bff.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import cl.duoc.ms_digitalfix_bff.dto.CambiarEstadoRequest;
import cl.duoc.ms_digitalfix_bff.dto.CrearWorkOrderRequest;
import cl.duoc.ms_digitalfix_bff.dto.EstadoOT;
import cl.duoc.ms_digitalfix_bff.dto.WorkOrder;
import cl.duoc.ms_digitalfix_bff.exception.NotFoundException;

/**
 * Almacenamiento en memoria, provisional.
 *
 * Cuando ms-digitalfix-workorders este operativo, esta clase pasa a ser un cliente
 * HTTP que reenvia al microservicio propagando el token. La interfaz publica que
 * consume el controlador no cambia.
 */
@Service
public class WorkOrderService {

    private final Map<String, WorkOrder> almacen = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger(0);

    public WorkOrderService() {
        cargarDatosDeEjemplo();
    }

    public List<WorkOrder> listar() {
        return almacen.values().stream()
                .sorted(Comparator.comparing(WorkOrder::creadaEn).reversed())
                .toList();
    }

    /** Un Cliente solo puede ver las ordenes que el mismo creo. */
    public List<WorkOrder> listarPorCreador(String creador) {
        return almacen.values().stream()
                .filter(ot -> ot.creadaPor().equals(creador))
                .sorted(Comparator.comparing(WorkOrder::creadaEn).reversed())
                .toList();
    }

    public WorkOrder obtener(String id) {
        WorkOrder ot = almacen.get(id);
        if (ot == null) {
            throw new NotFoundException("No existe la orden de trabajo " + id);
        }
        return ot;
    }

    public WorkOrder crear(CrearWorkOrderRequest request, String creador) {
        String id = siguienteId();
        LocalDateTime ahora = LocalDateTime.now();

        WorkOrder ot = new WorkOrder(
                id,
                request.cliente(),
                request.descripcion(),
                request.direccion(),
                EstadoOT.INGRESADA,
                null,
                creador,
                ahora,
                ahora);

        almacen.put(id, ot);
        return ot;
    }

    public WorkOrder cambiarEstado(String id, CambiarEstadoRequest request) {
        WorkOrder actual = obtener(id);

        WorkOrder actualizada = new WorkOrder(
                actual.id(),
                actual.cliente(),
                actual.descripcion(),
                actual.direccion(),
                request.estado(),
                request.tecnicoAsignado() != null
                        ? request.tecnicoAsignado()
                        : actual.tecnicoAsignado(),
                actual.creadaPor(),
                actual.creadaEn(),
                LocalDateTime.now());

        almacen.put(id, actualizada);
        return actualizada;
    }

    public void eliminar(String id) {
        if (almacen.remove(id) == null) {
            throw new NotFoundException("No existe la orden de trabajo " + id);
        }
    }

    private String siguienteId() {
        return "OT-%04d".formatted(secuencia.incrementAndGet());
    }

    private void cargarDatosDeEjemplo() {
        LocalDateTime ahora = LocalDateTime.now();

        almacen.put("OT-0001", new WorkOrder(
                "OT-0001",
                "Condominio Los Aromos",
                "Tablero general con corte intermitente en el bloque B",
                "Av. Los Aromos 1420, Lampa",
                EstadoOT.ASIGNADA,
                "Cuadrilla Norte",
                "sistema",
                ahora.minusDays(2),
                ahora.minusDays(1)));

        almacen.put("OT-0002", new WorkOrder(
                "OT-0002",
                "Panaderia San Miguel",
                "Recambio de automatico y revision de empalme",
                "Calle Principal 87, Colina",
                EstadoOT.INGRESADA,
                null,
                "sistema",
                ahora.minusDays(1),
                ahora.minusDays(1)));

        secuencia.set(2);
    }
}