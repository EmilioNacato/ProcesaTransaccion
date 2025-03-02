package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para información de gateways de pago")
public class GatewayDTO {
    
    @Schema(description = "Código único del gateway", example = "GW001")
    @NotBlank(message = "El código del gateway es requerido")
    @Size(min = 1, max = 10, message = "El código del gateway debe tener entre 1 y 10 caracteres")
    private String codGateway;
    
    @Schema(description = "Nombre del gateway", example = "Paymentez")
    @NotBlank(message = "El nombre del gateway es requerido")
    @Size(min = 1, max = 100, message = "El nombre del gateway debe tener entre 1 y 100 caracteres")
    private String nombre;
} 