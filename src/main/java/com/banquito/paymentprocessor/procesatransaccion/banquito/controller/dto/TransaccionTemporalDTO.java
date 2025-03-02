package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para transacciones temporales almacenadas en Redis")
public class TransaccionTemporalDTO {
    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codTransaccion;
    
    @Schema(description = "Número de tarjeta enmascarado", example = "4532XXXXXXXXXXXX")
    private String numeroTarjeta;
    
    @Schema(description = "Fecha y hora de la transacción")
    private LocalDateTime fechaTransaccion;
    
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
    
    @Schema(description = "Estado actual de la transacción", example = "VALIDADA")
    private String estado;
    
    @Schema(description = "Código SWIFT del banco emisor", example = "BANKECXXXX")
    private String swiftBanco;
} 