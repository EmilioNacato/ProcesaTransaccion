package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "Transacciones", description = "API para procesar transacciones de pago")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService service;

    @GetMapping
    @Operation(summary = "Obtiene todas las transacciones")
    public ResponseEntity<List<Transaccion>> obtenerTodasLasTransacciones() {
        log.debug("Obteniendo todas las transacciones");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una transacción por su ID")
    public ResponseEntity<Transaccion> obtenerTransaccion(@PathVariable Long id) {
        log.debug("Obteniendo transacción con id: {}", id);
        return ResponseEntity.ok(service.obtenerTransaccion(id));
    }

    @GetMapping("/tarjeta/{numeroTarjeta}")
    @Operation(summary = "Obtiene transacciones por número de tarjeta")
    public ResponseEntity<List<Transaccion>> obtenerTransaccionesPorTarjeta(@PathVariable String numeroTarjeta) {
        log.debug("Obteniendo transacciones para la tarjeta: {}", numeroTarjeta);
        return ResponseEntity.ok(service.findByNumeroTarjeta(numeroTarjeta));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtiene transacciones por estado")
    public ResponseEntity<List<Transaccion>> obtenerTransaccionesPorEstado(@PathVariable String estado) {
        log.debug("Obteniendo transacciones con estado: {}", estado);
        return ResponseEntity.ok(service.findByEstado(estado));
    }

    @PostMapping
    @Operation(summary = "Procesa una nueva transacción")
    public ResponseEntity<Transaccion> procesarTransaccion(@Valid @RequestBody Transaccion transaccion) {
        log.info("Procesando nueva transacción: {}", transaccion);
        return ResponseEntity.ok(service.procesarTransaccion(transaccion));
    }
} 