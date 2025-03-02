package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.TransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper.TransaccionMapper;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.TransaccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/transacciones")
@Tag(name = "Transacciones", description = "API para procesar y gestionar transacciones de pago")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService service;
    private final TransaccionMapper mapper;

    @PostMapping
    @Operation(
        summary = "Procesa una nueva transacción",
        description = "Recibe y procesa una nueva transacción de pago enviada desde el gateway. " +
                      "Valida la transacción contra servicios de fraude y marca, actualiza su estado " +
                      "y registra el historial correspondiente."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Transacción procesada exitosamente",
            content = @Content(schema = @Schema(implementation = TransaccionDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de la transacción inválidos"
        ),
        @ApiResponse(
            responseCode = "422", 
            description = "Error en el procesamiento de la transacción (fraude, tarjeta inválida, etc.)"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<TransaccionDTO> procesarTransaccion(
            @Parameter(description = "Datos de la transacción a procesar", required = true)
            @Valid @RequestBody TransaccionDTO transaccionDTO) {
        log.info("Procesando nueva transacción desde gateway: {}", transaccionDTO);
        try {
            Transaccion transaccion = mapper.toEntity(transaccionDTO);
            Transaccion resultado = service.procesarTransaccion(transaccion);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(resultado));
        } catch (TransaccionRechazadaException e) {
            log.warn("Transacción rechazada: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{codTransaccion}")
    @Operation(
        summary = "Obtiene una transacción por su código",
        description = "Busca y retorna una transacción específica basada en su código único de transacción. " +
                      "Primero verifica en Redis (transacciones recientes) y luego en la base de datos PostgreSQL."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Transacción encontrada exitosamente",
            content = @Content(schema = @Schema(implementation = TransaccionDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Transacción no encontrada"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<TransaccionDTO> obtenerTransaccionPorCodigo(
            @Parameter(description = "Código único de la transacción", required = true, example = "TRX1234567") 
            @PathVariable String codTransaccion) {
        log.debug("Obteniendo transacción con código: {}", codTransaccion);
        Transaccion transaccion = service.obtenerTransaccionPorCodigo(codTransaccion);
        return ResponseEntity.ok(mapper.toDTO(transaccion));
    }

    @GetMapping("/recientes")
    @Operation(
        summary = "Obtiene transacciones recientes",
        description = "Retorna las transacciones procesadas en un intervalo de tiempo especificado. " +
                      "Por defecto, retorna las transacciones de la última hora."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Transacciones obtenidas exitosamente"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<List<TransaccionDTO>> obtenerTransaccionesRecientes(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)", example = "2024-02-26T10:00:00") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)", example = "2024-02-26T11:00:00") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        // Si no se especifican fechas, buscar transacciones de la última hora
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaDesde = (desde != null) ? desde : ahora.minusHours(1);
        LocalDateTime fechaHasta = (hasta != null) ? hasta : ahora;
        
        log.debug("Obteniendo transacciones desde {} hasta {}", fechaDesde, fechaHasta);
        List<Transaccion> transacciones = service.buscarTransaccionesPorFecha(fechaDesde, fechaHasta);
        return ResponseEntity.ok(mapper.toDTOList(transacciones));
    }
} 