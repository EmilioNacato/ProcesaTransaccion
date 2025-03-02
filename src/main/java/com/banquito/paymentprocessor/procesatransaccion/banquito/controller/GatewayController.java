package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.GatewayDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper.GatewayMapper;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.ErrorResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Gateway;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.GatewayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/gateway")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gateway", description = "API para gestionar los gateways de pago")
public class GatewayController {

    private final GatewayService service;
    private final GatewayMapper mapper;

    @GetMapping
    @Operation(
        summary = "Listar todos los gateways",
        description = "Obtiene la lista completa de gateways de pago disponibles"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de gateways obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = GatewayDTO.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<GatewayDTO>> listarGateways() {
        log.info("Listando todos los gateways");
        List<Gateway> gateways = service.findAll();
        return ResponseEntity.ok(mapper.toDTOList(gateways));
    }

    @GetMapping("/{codGateway}")
    @Operation(
        summary = "Obtener gateway por código",
        description = "Obtiene los detalles de un gateway específico usando su código único"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Gateway encontrado exitosamente",
            content = @Content(schema = @Schema(implementation = GatewayDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Gateway no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<GatewayDTO> obtenerGateway(
            @Parameter(description = "Código único del gateway", required = true, example = "PAYPAL")
            @PathVariable String codGateway) {
        log.info("Buscando gateway con código: {}", codGateway);
        Gateway gateway = service.findByCodigo(codGateway);
        return ResponseEntity.ok(mapper.toDTO(gateway));
    }

    @PostMapping
    @Operation(
        summary = "Crear nuevo gateway",
        description = "Registra un nuevo gateway de pago en el sistema"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Gateway creado exitosamente",
            content = @Content(schema = @Schema(implementation = GatewayDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos del gateway inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<GatewayDTO> crearGateway(
            @Parameter(description = "Datos del nuevo gateway", required = true)
            @Valid @RequestBody GatewayDTO gatewayDTO) {
        log.info("Creando nuevo gateway: {}", gatewayDTO);
        Gateway gateway = service.create(mapper.toModel(gatewayDTO));
        return new ResponseEntity<>(mapper.toDTO(gateway), HttpStatus.CREATED);
    }

    @PutMapping("/{codGateway}")
    @Operation(
        summary = "Actualizar gateway existente",
        description = "Actualiza la información de un gateway existente identificado por su código"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Gateway actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = GatewayDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos del gateway inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Gateway no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<GatewayDTO> actualizarGateway(
            @Parameter(description = "Código único del gateway a actualizar", required = true, example = "PAYPAL")
            @PathVariable String codGateway,
            @Parameter(description = "Datos actualizados del gateway", required = true)
            @Valid @RequestBody GatewayDTO gatewayDTO) {
        log.info("Actualizando gateway con código: {}", codGateway);
        
        // Aseguramos que el código del path coincida con el del DTO
        if (!codGateway.equals(gatewayDTO.getCodGateway())) {
            gatewayDTO.setCodGateway(codGateway);
        }
        
        Gateway gateway = service.update(codGateway, mapper.toModel(gatewayDTO));
        return ResponseEntity.ok(mapper.toDTO(gateway));
    }

    @DeleteMapping("/{codGateway}")
    @Operation(
        summary = "Eliminar gateway",
        description = "Elimina un gateway existente del sistema"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Gateway eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Gateway no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> eliminarGateway(
            @Parameter(description = "Código único del gateway a eliminar", required = true, example = "PAYPAL")
            @PathVariable String codGateway) {
        log.info("Eliminando gateway con código: {}", codGateway);
        service.delete(codGateway);
        return ResponseEntity.noContent().build();
    }
} 