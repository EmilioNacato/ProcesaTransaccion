package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import lombok.Data;

@Data
public class ValidacionFraudeResponseDTO {
    private boolean esFraude;
    private String codigoRegla;
    private String mensaje;
} 