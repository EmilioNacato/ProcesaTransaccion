package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para solicitud de validación de fraude
 * Ajustado para coincidir con lo que espera el servicio validafraude
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para validar posible fraude en una transacción")
public class ValidacionFraudeRequest {
    
    @Schema(description = "Número de tarjeta a validar", example = "4532123456789012")
    private String numeroTarjeta;
    
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
    
    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codTransaccion;
    
    @Schema(description = "Código SWIFT del banco", example = "BANKEC21")
    private String swiftBanco;
    
    @Schema(description = "Código del comercio", example = "COM12345")
    private String codigoComercio;
    
    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codigoUnico;
    
    @Schema(description = "Tipo de transacción", example = "Compra")
    private String tipoTransaccion;
} 