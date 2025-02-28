package com.banquito.paymentprocessor.procesatransaccion.banquito.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatewayDTO {
    
    @NotBlank(message = "El código del gateway es obligatorio")
    @Size(max = 10, message = "El código del gateway no puede tener más de 10 caracteres")
    private String codGateway;

    @NotBlank(message = "El nombre del gateway es obligatorio")
    @Size(max = 100, message = "El nombre del gateway no puede tener más de 100 caracteres")
    private String nombre;
} 