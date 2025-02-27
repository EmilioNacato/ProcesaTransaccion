package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.HistorialEstadoTransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper.HistorialEstadoTransaccionMapper;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.HistorialEstadoTransaccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/historiales")
@Tag(name = "Historial de Estados", description = "API para gestionar el historial de estados de las transacciones")
@Slf4j
public class HistorialEstadoTransaccionController {

    private final HistorialEstadoTransaccionService service;
    private final HistorialEstadoTransaccionMapper mapper;

    public HistorialEstadoTransaccionController(HistorialEstadoTransaccionService service, 
            HistorialEstadoTransaccionMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(
        summary = "Obtiene todos los historiales de estado",
        description = "Retorna una lista de todos los historiales de estado de transacciones registrados"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de historiales obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> obtenerTodosLosHistoriales() {
        log.debug("Obteniendo todos los historiales de estado");
        List<HistorialEstadoTransaccionDTO> historiales = this.service.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene un historial por su ID",
        description = "Busca y retorna un historial de estado específico basado en su identificador único"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial encontrado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Historial no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<HistorialEstadoTransaccionDTO> obtenerHistorialPorId(
            @Parameter(description = "ID del historial", required = true)
            @PathVariable String id) {
        log.debug("Obteniendo historial con id: {}", id);
        return ResponseEntity.ok(mapper.toDTO(this.service.findById(id)));
    }

    @GetMapping("/transaccion/{codTransaccion}")
    @Operation(
        summary = "Obtiene historiales por código de transacción",
        description = "Retorna todos los historiales de estado asociados a una transacción específica"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historiales encontrados exitosamente"),
        @ApiResponse(responseCode = "404", description = "No se encontraron historiales para la transacción"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> obtenerHistorialesPorTransaccion(
            @Parameter(description = "Código de la transacción", required = true)
            @PathVariable String codTransaccion) {
        log.debug("Obteniendo historiales para la transacción: {}", codTransaccion);
        List<HistorialEstadoTransaccionDTO> historiales = this.service.findByCodTransaccion(codTransaccion).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/estado/{estado}")
    @Operation(
        summary = "Obtiene historiales por estado",
        description = "Retorna todos los historiales que corresponden a un estado específico"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historiales encontrados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Estado inválido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> obtenerHistorialesPorEstado(
            @Parameter(description = "Estado de la transacción", required = true)
            @PathVariable String estado) {
        log.debug("Obteniendo historiales con estado: {}", estado);
        List<HistorialEstadoTransaccionDTO> historiales = this.service.findByEstado(estado).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiales);
    }

    @PostMapping
    @Operation(
        summary = "Crea un nuevo historial de estado",
        description = "Registra un nuevo historial de estado para una transacción"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del historial inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<HistorialEstadoTransaccionDTO> crearHistorial(
            @Parameter(description = "Datos del historial de estado", required = true)
            @Valid @RequestBody HistorialEstadoTransaccionDTO historialDTO) {
        log.debug("Creando nuevo historial de estado");
        HistorialEstadoTransaccion historial = this.service.create(mapper.toModel(historialDTO));
        return ResponseEntity.ok(mapper.toDTO(historial));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Elimina un historial de estado",
        description = "Elimina un historial de estado específico basado en su identificador único"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Historial eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Historial no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> eliminarHistorial(
            @Parameter(description = "ID del historial a eliminar", required = true)
            @PathVariable String id) {
        log.debug("Eliminando historial con id: {}", id);
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
} 