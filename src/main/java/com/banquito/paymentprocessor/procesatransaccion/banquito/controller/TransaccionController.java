package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/transacciones")
@Tag(name = "Transacciones", description = "API para procesar y gestionar transacciones de pago")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService service;

    @GetMapping
    @Operation(
        summary = "Obtiene todas las transacciones",
        description = "Retorna una lista de todas las transacciones registradas en el sistema"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de transacciones obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<Transaccion>> obtenerTodasLasTransacciones() {
        log.debug("Obteniendo todas las transacciones");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene una transacción por su ID",
        description = "Busca y retorna una transacción específica basada en su identificador único"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacción encontrada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Transaccion> obtenerTransaccion(
            @Parameter(description = "ID de la transacción", required = true) 
            @PathVariable Long id) {
        log.debug("Obteniendo transacción con id: {}", id);
        return ResponseEntity.ok(service.obtenerTransaccion(id));
    }

    @GetMapping("/tarjeta/{numeroTarjeta}")
    @Operation(
        summary = "Obtiene transacciones por número de tarjeta",
        description = "Retorna todas las transacciones asociadas a un número de tarjeta específico"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacciones encontradas exitosamente"),
        @ApiResponse(responseCode = "404", description = "No se encontraron transacciones para la tarjeta"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<Transaccion>> obtenerTransaccionesPorTarjeta(
            @Parameter(description = "Número de tarjeta", required = true) 
            @PathVariable String numeroTarjeta) {
        log.debug("Obteniendo transacciones para la tarjeta: {}", numeroTarjeta);
        return ResponseEntity.ok(service.findByNumeroTarjeta(numeroTarjeta));
    }

    @GetMapping("/estado/{estado}")
    @Operation(
        summary = "Obtiene transacciones por estado",
        description = "Retorna todas las transacciones que se encuentran en un estado específico"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacciones encontradas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Estado de transacción inválido"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<Transaccion>> obtenerTransaccionesPorEstado(
            @Parameter(description = "Estado de la transacción (PENDIENTE, APROBADA, RECHAZADA)", required = true) 
            @PathVariable String estado) {
        log.debug("Obteniendo transacciones con estado: {}", estado);
        return ResponseEntity.ok(service.findByEstado(estado));
    }

    @PostMapping
    @Operation(
        summary = "Procesa una nueva transacción",
        description = "Crea y procesa una nueva transacción de pago en el sistema"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacción procesada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de la transacción inválidos"),
        @ApiResponse(responseCode = "422", description = "Error en el procesamiento de la transacción"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Transaccion> procesarTransaccion(
            @Parameter(description = "Datos de la transacción", required = true)
            @Valid @RequestBody Transaccion transaccion) {
        log.info("Procesando nueva transacción: {}", transaccion);
        return ResponseEntity.ok(service.procesarTransaccion(transaccion));
    }
} 