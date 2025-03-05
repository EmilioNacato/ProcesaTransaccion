package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.TransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper.TransaccionMapper;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.TransaccionService;
import com.banquito.paymentprocessor.procesatransaccion.banquito.context.TransaccionContextHolder;

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
    @Operation(summary = "Procesar una transacción", description = "Procesa una nueva transacción con validaciones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transacción procesada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de transacción inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar transacción")
    })
    public ResponseEntity<Object> procesarTransaccion(
            @RequestBody @Valid TransaccionDTO transaccionDTO) {
        log.info("Recibida solicitud para procesar una nueva transacción");
        
        try {
            Transaccion transaccion = mapper.toEntity(transaccionDTO);
            
            // Capturar los valores de diferido y cuotas que no se mapean automáticamente
            // pero son necesarios para procesarcores
            Boolean esDiferido = transaccionDTO.getDiferido();
            Integer numCuotas = transaccionDTO.getCuotas();
            
            // Establecer estos valores en el objeto de contexto
            TransaccionContextHolder.setDiferido(esDiferido);
            TransaccionContextHolder.setCuotas(numCuotas);
            
            Transaccion resultado = service.procesarTransaccion(transaccion);
            
            // Limpiar el contexto
            TransaccionContextHolder.clear();
            
            String mensaje;
            HttpStatus status;
            String detalleEstado = service.obtenerUltimoMensaje(resultado);
            
            // Definir mensaje según el estado final de la transacción
            if (TransaccionService.ESTADO_COMPLETADA.equals(resultado.getEstado())) {
                mensaje = "Transacción completada exitosamente. Código: " + resultado.getCodTransaccion();
                status = HttpStatus.OK;
            } else if (TransaccionService.ESTADO_RECHAZADA.equals(resultado.getEstado())) {
                if (detalleEstado != null && !detalleEstado.isEmpty()) {
                    mensaje = "Transacción rechazada: " + detalleEstado;
                } else {
                    mensaje = "Transacción rechazada: Validación fallida";
                }
                status = HttpStatus.OK;
            } else if (TransaccionService.ESTADO_ERROR.equals(resultado.getEstado())) {
                if (detalleEstado != null && !detalleEstado.isEmpty()) {
                    mensaje = "Error al procesar transacción: " + detalleEstado;
                } else {
                    mensaje = "Error al procesar transacción";
                }
                status = HttpStatus.OK;
            } else if (TransaccionService.ESTADO_FRAUDE.equals(resultado.getEstado())) {
                if (detalleEstado != null && !detalleEstado.isEmpty()) {
                    mensaje = "Transacción identificada como posible fraude: " + detalleEstado;
                } else {
                    mensaje = "Transacción identificada como posible fraude";
                }
                status = HttpStatus.OK;
            } else {
                if (detalleEstado != null && !detalleEstado.isEmpty()) {
                    mensaje = "Transacción en estado " + resultado.getEstado() + ": " + detalleEstado;
                } else {
                    mensaje = "Transacción en estado: " + resultado.getEstado();
                }
                status = HttpStatus.OK;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", mensaje);
            response.put("codTransaccion", resultado.getCodTransaccion());
            response.put("estado", resultado.getEstado());
            
            return new ResponseEntity<>(response, status);
            
        } catch (TransaccionRechazadaException e) {
            log.warn("Transacción rechazada: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Transacción rechazada: " + e.getMessage());
            response.put("estado", "RECHAZADA");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al procesar transacción: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al procesar transacción: " + e.getMessage());
            response.put("estado", "ERROR");
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
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