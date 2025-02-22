package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Respuesta del procesamiento bancario de una transacción")
public class ProcesoBancarioResponse {
    
    @Schema(description = "Indica si la transacción fue exitosa", example = "true")
    private Boolean transaccionExitosa;
    
    @Schema(description = "Código de autorización bancaria", example = "AUTH123456")
    private String codigoAutorizacion;
    
    @Schema(description = "Mensaje informativo del proceso", example = "Transacción procesada correctamente")
    private String mensaje;
} 