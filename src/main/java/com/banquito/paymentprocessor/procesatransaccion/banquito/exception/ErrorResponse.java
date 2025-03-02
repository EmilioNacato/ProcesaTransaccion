package com.banquito.paymentprocessor.procesatransaccion.banquito.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estructura de respuesta para errores")
public class ErrorResponse {
    
    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;
    
    @Schema(description = "Mensaje detallado del error", example = "El recurso solicitado no fue encontrado")
    private String message;
    
    @Schema(description = "Marca de tiempo cuando ocurrió el error", example = "2024-04-15T10:15:30")
    private String timestamp;
    
    @Schema(description = "Detalles adicionales sobre la petición", example = "uri=/api/v1/gateway/1")
    private String path;
} 