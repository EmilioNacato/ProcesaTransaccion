package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.TransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper.TransaccionMapper;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.TransaccionService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/transacciones")
public class TransaccionController {

    private final TransaccionService service;
    private final TransaccionMapper mapper;

    public TransaccionController(TransaccionService service, TransaccionMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<TransaccionDTO>> obtenerTodasLasTransacciones() {
        log.debug("Obteniendo todas las transacciones");
        List<TransaccionDTO> transacciones = this.service.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transacciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDTO> obtenerTransaccionPorId(@PathVariable String id) {
        log.debug("Obteniendo transacción con id: {}", id);
        return ResponseEntity.ok(mapper.toDTO(this.service.findById(id)));
    }

    @GetMapping("/tarjeta/{numeroTarjeta}")
    public ResponseEntity<List<TransaccionDTO>> obtenerTransaccionesPorTarjeta(@PathVariable String numeroTarjeta) {
        log.debug("Obteniendo transacciones para la tarjeta: {}", numeroTarjeta);
        List<TransaccionDTO> transacciones = this.service.findByNumeroTarjeta(numeroTarjeta).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transacciones);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<TransaccionDTO>> obtenerTransaccionesPorEstado(@PathVariable String estado) {
        log.debug("Obteniendo transacciones con estado: {}", estado);
        List<TransaccionDTO> transacciones = this.service.findByEstado(estado).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transacciones);
    }

    @PostMapping
    public ResponseEntity<TransaccionDTO> crearTransaccion(@Valid @RequestBody TransaccionDTO transaccionDTO) {
        log.debug("Creando nueva transacción");
        Transaccion transaccion = this.service.create(mapper.toModel(transaccionDTO));
        return ResponseEntity.ok(mapper.toDTO(transaccion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransaccionDTO> actualizarTransaccion(@PathVariable String id, 
            @Valid @RequestBody TransaccionDTO transaccionDTO) {
        log.debug("Actualizando transacción con id: {}", id);
        Transaccion transaccion = this.service.update(id, mapper.toModel(transaccionDTO));
        return ResponseEntity.ok(mapper.toDTO(transaccion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTransaccion(@PathVariable String id) {
        log.debug("Eliminando transacción con id: {}", id);
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
} 