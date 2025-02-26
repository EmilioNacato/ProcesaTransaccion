package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.TransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper.TransaccionMapper;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.TransaccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "Transacciones", description = "API para procesar transacciones de pago")
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
    public ResponseEntity<TransaccionDTO> obtenerTransaccionPorId(@PathVariable Long id) {
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

    @PostMapping("/procesar")
    @Operation(summary = "Procesa una nueva transacción", 
              description = "Recibe y procesa una nueva transacción de pago, validando los datos y actualizando su estado")
    public ResponseEntity<TransaccionDTO> procesarTransaccion(@Valid @RequestBody TransaccionDTO transaccionDTO) {
        log.info("Procesando nueva transacción: {}", transaccionDTO);
        return ResponseEntity.ok(
            mapper.toDTO(
                service.procesarTransaccion(
                    mapper.toModel(transaccionDTO)
                )
            )
        );
    }
} 