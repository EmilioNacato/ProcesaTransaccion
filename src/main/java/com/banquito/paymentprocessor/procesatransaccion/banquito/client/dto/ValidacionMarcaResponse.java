package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Respuesta de la validación de una tarjeta con su marca")
public class ValidacionMarcaResponse {
    
    @Schema(description = "Indica si la tarjeta es válida", example = "true")
    private Boolean tarjetaValida;
    
    @Schema(description = "Código SWIFT del banco", example = "BANKEC21")
    private String swiftBanco;
    
    @Schema(description = "Mensaje informativo del proceso", example = "Tarjeta validada correctamente")
    private String mensaje;
} 