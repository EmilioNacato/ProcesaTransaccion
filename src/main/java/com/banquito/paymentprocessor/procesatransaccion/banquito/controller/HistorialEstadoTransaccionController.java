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

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/historiales")
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
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> obtenerTodosLosHistoriales() {
        log.debug("Obteniendo todos los historiales de estado");
        List<HistorialEstadoTransaccionDTO> historiales = this.service.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialEstadoTransaccionDTO> obtenerHistorialPorId(@PathVariable String id) {
        log.debug("Obteniendo historial con id: {}", id);
        return ResponseEntity.ok(mapper.toDTO(this.service.findById(id)));
    }

    @GetMapping("/transaccion/{codTransaccion}")
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> obtenerHistorialesPorTransaccion(
            @PathVariable String codTransaccion) {
        log.debug("Obteniendo historiales para la transacción: {}", codTransaccion);
        List<HistorialEstadoTransaccionDTO> historiales = this.service.findByCodTransaccion(codTransaccion).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> obtenerHistorialesPorEstado(@PathVariable String estado) {
        log.debug("Obteniendo historiales con estado: {}", estado);
        List<HistorialEstadoTransaccionDTO> historiales = this.service.findByEstado(estado).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiales);
    }

    @PostMapping
    public ResponseEntity<HistorialEstadoTransaccionDTO> crearHistorial(
            @Valid @RequestBody HistorialEstadoTransaccionDTO historialDTO) {
        log.debug("Creando nuevo historial de estado");
        HistorialEstadoTransaccion historial = this.service.create(mapper.toModel(historialDTO));
        return ResponseEntity.ok(mapper.toDTO(historial));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHistorial(@PathVariable String id) {
        log.debug("Eliminando historial con id: {}", id);
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
} 