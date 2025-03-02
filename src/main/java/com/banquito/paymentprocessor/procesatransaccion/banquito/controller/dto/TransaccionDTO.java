package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para recibir y enviar información de transacciones")
public class TransaccionDTO {
    
    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codTransaccion;
    
    @Schema(description = "Código único para identificar la transacción", example = "TRANS20240301123456")
    private String codigoUnico;

    @NotBlank(message = "El número de tarjeta es requerido")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe contener solo números")
    @Schema(description = "Número de tarjeta de 16 dígitos", example = "4532123456789012")
    private String numeroTarjeta;

    @NotBlank(message = "El CVV es requerido")
    @Size(min = 3, max = 4, message = "El CVV debe tener entre 3 y 4 dígitos")
    @Schema(description = "Código de seguridad de la tarjeta", example = "123")
    private String cvv;
    
    @NotBlank(message = "La fecha de caducidad es requerida")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Formato de fecha inválido (MM/YY)")
    @Schema(description = "Fecha de caducidad de la tarjeta", example = "12/25")
    private String fechaCaducidad;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
    
    @Schema(description = "Código de moneda", example = "USD")
    private String codigoMoneda;
    
    @Schema(description = "Marca de la tarjeta", example = "VISA")
    private String marca;

    @Schema(description = "Fecha de la transacción")
    private LocalDateTime fechaTransaccion;

    @Schema(description = "Estado actual de la transacción", example = "PEN")
    private String estado;
    
    @Schema(description = "Referencia de la transacción", example = "Compra en línea")
    private String referencia;
    
    @Schema(description = "País de la transacción", example = "EC")
    private String pais;
    
    @Schema(description = "Tipo de transacción", example = "COM")
    private String tipo;
    
    @Schema(description = "SWIFT del banco del comercio", example = "BANKECXXXX")
    private String swiftBancoComercio;
    
    @Schema(description = "Cuenta IBAN del comercio", example = "EC1234567890123456789012")
    private String cuentaIbanComercio;

    @Schema(description = "SWIFT del banco de la tarjeta", example = "BANKUS33XXX")
    private String swiftBancoTarjeta;
} 