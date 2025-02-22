package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para procesar una transacción con el banco")
public class ProcesoBancarioRequest {
    
    @Schema(description = "Número de tarjeta", example = "4532123456789012")
    private String numeroTarjeta;
    
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
    
    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codigoTransaccion;
    
    @Schema(description = "Código SWIFT del banco", example = "BANKEC21")
    private String swiftBanco;
    
    @Schema(description = "Referencia de la transacción", example = "REF-TRX1234567")
    private String referencia;
} 