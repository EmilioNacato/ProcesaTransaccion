package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.GatewayDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Gateway;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.GatewayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/gateways")
@RequiredArgsConstructor
@Tag(name = "Gateway", description = "API para la gestión de gateways de pago")
public class GatewayController {
    
    private final GatewayService gatewayService;

    @Operation(summary = "Obtener todos los gateways", description = "Retorna una lista de todos los gateways disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de gateways obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<Gateway>> findAll() {
        return ResponseEntity.ok(this.gatewayService.findAll());
    }

    @Operation(summary = "Obtener un gateway por código", description = "Retorna un gateway específico basado en su código")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gateway encontrado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Gateway no encontrado")
    })
    @GetMapping("/{codGateway}")
    public ResponseEntity<Gateway> findById(
            @Parameter(description = "Código del gateway", required = true) 
            @PathVariable String codGateway) {
        return this.gatewayService.findById(codGateway)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un nuevo gateway", description = "Crea un nuevo gateway con los datos proporcionados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gateway creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del gateway inválidos")
    })
    @PostMapping
    public ResponseEntity<Gateway> create(
            @Parameter(description = "Datos del gateway", required = true)
            @Valid @RequestBody GatewayDTO gatewayDTO) {
        Gateway gateway = new Gateway();
        gateway.setCodGateway(gatewayDTO.getCodGateway());
        gateway.setNombre(gatewayDTO.getNombre());
        return ResponseEntity.ok(this.gatewayService.save(gateway));
    }

    @Operation(summary = "Actualizar un gateway existente", description = "Actualiza los datos de un gateway existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gateway actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Gateway no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos del gateway inválidos")
    })
    @PutMapping("/{codGateway}")
    public ResponseEntity<Gateway> update(
            @Parameter(description = "Código del gateway a actualizar", required = true)
            @PathVariable String codGateway, 
            @Parameter(description = "Nuevos datos del gateway", required = true)
            @Valid @RequestBody GatewayDTO gatewayDTO) {
        return this.gatewayService.findById(codGateway)
                .map(gateway -> {
                    gateway.setNombre(gatewayDTO.getNombre());
                    return ResponseEntity.ok(this.gatewayService.save(gateway));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un gateway", description = "Elimina un gateway existente por su código")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gateway eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Gateway no encontrado")
    })
    @DeleteMapping("/{codGateway}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Código del gateway a eliminar", required = true)
            @PathVariable String codGateway) {
        if (this.gatewayService.findById(codGateway).isPresent()) {
            this.gatewayService.deleteById(codGateway);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
} 