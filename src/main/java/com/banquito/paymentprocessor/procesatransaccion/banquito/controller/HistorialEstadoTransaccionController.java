package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.HistorialEstadoTransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper.HistorialEstadoTransaccionMapper;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.HistorialEstadoTransaccionService;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/historial")
@Tag(name = "Historial", description = "API para consultar el historial de estados de las transacciones")
@Slf4j
@RequiredArgsConstructor
public class HistorialEstadoTransaccionController {

    private final HistorialEstadoTransaccionService service;
    private final HistorialEstadoTransaccionMapper mapper;

    @GetMapping("/transaccion/{codTransaccion}")
    @Operation(
        summary = "Consulta historial de una transacción",
        description = "Obtiene el historial completo de estados por los que ha pasado una transacción específica, " +
                      "ordenados desde el más reciente al más antiguo."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Historial obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = HistorialEstadoTransaccionDTO.class))
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
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> obtenerHistorialTransaccion(
            @Parameter(description = "Código único de la transacción", required = true, example = "TRX1234567") 
            @PathVariable String codTransaccion) {
        log.debug("Consultando historial de la transacción: {}", codTransaccion);
        List<HistorialEstadoTransaccionDTO> historiales = service.findByCodTransaccion(codTransaccion).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/estado")
    @Operation(
        summary = "Consulta transacciones por estado",
        description = "Obtiene el historial de todas las transacciones que se encuentran o han pasado por un estado específico, " +
                      "opcionalmente limitando los resultados con un parámetro de cantidad."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Consulta realizada exitosamente"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Parámetro de estado inválido"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> buscarPorEstado(
            @Parameter(description = "Estado a consultar (PENDIENTE, VALIDADA, APROBADA, RECHAZADA, FRAUDE, ERROR)", 
                      required = true, example = "FRAUDE") 
            @RequestParam String estado,
            
            @Parameter(description = "Cantidad máxima de registros a retornar (opcional)", example = "10")
            @RequestParam(required = false, defaultValue = "50") Integer limite) {
        log.debug("Buscando historial de transacciones con estado: {}, límite: {}", estado, limite);
        
        List<HistorialEstadoTransaccionDTO> historiales = service.findByEstado(estado).stream()
                .limit(limite)
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/recientes")
    @Operation(
        summary = "Consulta historial de transacciones por intervalo de tiempo",
        description = "Obtiene el historial de estados de transacciones en un intervalo de tiempo específico. " +
                      "Por defecto, retorna el historial de la última hora."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Consulta realizada exitosamente"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<List<HistorialEstadoTransaccionDTO>> buscarPorIntervaloTiempo(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)", example = "2024-02-26T10:00:00") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)", example = "2024-02-26T11:00:00") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        
        // Si no se especifican fechas, buscar historial de la última hora
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaDesde = (desde != null) ? desde : ahora.minusHours(1);
        LocalDateTime fechaHasta = (hasta != null) ? hasta : ahora;
        
        log.debug("Buscando historial desde {} hasta {}", fechaDesde, fechaHasta);
        List<HistorialEstadoTransaccionDTO> historiales = service.findByFechaEstadoCambioBetween(fechaDesde, fechaHasta).stream()
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
        this.service.delete(Long.valueOf(id));
        return ResponseEntity.noContent().build();
    }
} 