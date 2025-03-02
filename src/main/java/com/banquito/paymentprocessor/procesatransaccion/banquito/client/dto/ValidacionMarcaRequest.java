package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para validar una tarjeta con su marca")
public class ValidacionMarcaRequest {
    
    @Schema(description = "Número de tarjeta a validar", example = "4532123456789012")
    private String numeroTarjeta;
    
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
    
    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codigoTransaccion;
    
    @Schema(description = "Marca de la tarjeta", example = "VISA")
    private String marca;
    
    @Schema(description = "Código de seguridad de la tarjeta", example = "123")
    private String cvv;
    
    @Schema(description = "Fecha de caducidad de la tarjeta en formato MM/YY", example = "12/25")
    private String fechaCaducidad;
} 