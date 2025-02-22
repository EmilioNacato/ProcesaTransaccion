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

    @NotBlank(message = "El número de tarjeta es requerido")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe contener solo números")
    @Schema(description = "Número de tarjeta de 16 dígitos", example = "4532123456789012")
    private String numeroTarjeta;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;

    @Schema(description = "Fecha de la transacción")
    private LocalDateTime fechaTransaccion;

    @Schema(description = "Estado actual de la transacción", example = "PEN")
    private String estado;

    @Schema(description = "Código SWIFT del banco", example = "BANKUS33XXX")
    private String swiftBanco;
} 