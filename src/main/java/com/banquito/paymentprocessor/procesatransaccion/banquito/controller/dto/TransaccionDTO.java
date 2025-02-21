package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransaccionDTO {
    
    private String codTransaccion;

    @NotBlank(message = "El número de tarjeta es requerido")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe contener solo dígitos")
    private String numeroTarjeta;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @DecimalMax(value = "999999999999999.99", message = "El monto excede el límite permitido")
    private BigDecimal monto;

    private LocalDateTime fechaTransaccion;

    @Pattern(regexp = "PEN|PRO|APR|REJ", message = "El estado debe ser PEN (Pendiente), PRO (Procesando), APR (Aprobado) o REJ (Rechazado)")
    private String estado;

    @Size(max = 11, message = "El SWIFT del banco no puede exceder 11 caracteres")
    private String swiftBanco;
} 