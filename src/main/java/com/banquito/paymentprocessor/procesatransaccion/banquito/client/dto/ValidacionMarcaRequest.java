package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"codigoSeguridad"}) // Por seguridad no mostramos el CVV en logs
@Schema(description = "Solicitud para validar una tarjeta con su marca")
public class ValidacionMarcaRequest {
    
    @Schema(description = "Código único de la transacción", example = "TRX123456789")
    private String codigoUnicoTransaccion;
    
    @Schema(description = "Número de tarjeta a validar", example = "4532123456789012")
    private String numeroTarjeta;
    
    @Schema(description = "Código de seguridad de la tarjeta", example = "123")
    private String codigoSeguridad;
    
    @Schema(description = "Fecha de caducidad de la tarjeta en formato MM/YY", example = "12/25")
    private String fechaExpiracion;
    
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
} 