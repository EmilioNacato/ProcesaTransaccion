package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Respuesta de la validación de fraude de una transacción")
public class ValidacionFraudeResponse {
    
    @Schema(description = "Indica si la transacción es válida", example = "true")
    private Boolean transaccionValida;
    
    @Schema(description = "Mensaje informativo del proceso", example = "No se detectó fraude")
    private String mensaje;
    
    @Schema(description = "Código de respuesta", example = "200")
    private String codigoRespuesta;
} 