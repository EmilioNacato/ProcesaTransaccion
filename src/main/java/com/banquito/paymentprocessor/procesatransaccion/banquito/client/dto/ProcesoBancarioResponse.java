package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta del procesamiento bancario de una transacción")
public class ProcesoBancarioResponse {
    
    @Schema(description = "Código único de la transacción", example = "TRX123456")
    private String codigoUnico;
    
    @Schema(description = "Fecha y hora del procesamiento")
    private LocalDateTime fechaProceso;
    
    @Schema(description = "Estado de la transacción", example = "APROBADO")
    private String estado;
    
    @Schema(description = "Mensaje informativo del proceso", example = "Transacción procesada correctamente")
    private String mensaje;
    
    @Schema(description = "Código de respuesta del banco", example = "00")
    private String codigoRespuesta;
    
    @Schema(description = "Código SWIFT del banco", example = "BANKEC21")
    private String swiftBanco;
} 