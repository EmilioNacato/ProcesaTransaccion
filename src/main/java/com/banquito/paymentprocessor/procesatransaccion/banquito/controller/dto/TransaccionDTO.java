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
    
    @Schema(description = "Código único de la transacción generado internamente", example = "TRX1234567")
    private String codTransaccion;
    
    @Schema(description = "Código único para identificar la transacción", example = "TRX-2023-002")
    private String codigoUnicoTransaccion;

    @NotBlank(message = "El código de gateway es requerido")
    @Size(min = 1, max = 10, message = "El código de gateway debe tener entre 1 y 10 caracteres")
    @Schema(description = "Código del gateway que envía la transacción (solo para validación)", example = "PAYPAL")
    private String codigoGtw;

    @NotBlank(message = "El número de tarjeta es requerido")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe contener solo números")
    @Schema(description = "Número de tarjeta de 16 dígitos", example = "5555555555554444")
    private String numeroTarjeta;

    @NotNull(message = "El código de seguridad es requerido")
    @Schema(description = "Código de seguridad de la tarjeta", example = "456")
    private Integer codigoSeguridad;
    
    @NotBlank(message = "La fecha de expiración es requerida")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Formato de fecha inválido (MM/YY)")
    @Schema(description = "Fecha de expiración de la tarjeta", example = "10/26")
    private String fechaExpiracion;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Schema(description = "Monto de la transacción", example = "250.75")
    private BigDecimal monto;
    
    @Schema(description = "Código de moneda", example = "USD")
    private String moneda;
    
    @Schema(description = "Marca de la tarjeta", example = "MAST")
    private String marca;

    @Schema(description = "Fecha de la transacción (generada automáticamente)")
    private LocalDateTime fechaTransaccion;

    @Schema(description = "Estado actual de la transacción", example = "PEN")
    private String estado;
    
    @Schema(description = "Referencia de la transacción", example = "Compra en linea")
    private String referencia;
    
    @Schema(description = "País de la transacción", example = "EC")
    private String pais;
    
    @Schema(description = "Tipo de transacción", example = "PAG")
    private String tipo;
    
    @Schema(description = "Modalidad de la transacción", example = "DIF")
    private String modalidad;
    
    @Schema(description = "SWIFT del banco", example = "BSCHESMM")
    private String swift_banco;
    
    @Schema(description = "Cuenta IBAN", example = "ES9121000418450200051332")
    private String cuenta_iban;

    @Schema(description = "Nombre del titular de la tarjeta", example = "María López")
    private String nombreTitular;
    
    @Schema(description = "Transacción encriptada", example = "daaddaasd")
    private String transaccion_encriptada;
} 