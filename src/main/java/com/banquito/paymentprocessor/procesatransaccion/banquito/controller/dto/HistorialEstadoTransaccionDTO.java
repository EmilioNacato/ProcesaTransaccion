package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HistorialEstadoTransaccionDTO {
    
    private String codHistorialEstado;

    @NotBlank(message = "El código de transacción es requerido")
    @Size(max = 10, message = "El código de transacción no puede exceder 10 caracteres")
    private String codTransaccion;

    @NotBlank(message = "El estado es requerido")
    @Pattern(regexp = "PEN|PRO|APR|REJ", message = "El estado debe ser PEN (Pendiente), PRO (Procesando), APR (Aprobado) o REJ (Rechazado)")
    private String estado;

    private LocalDateTime fechaEstadoCambio;
} 