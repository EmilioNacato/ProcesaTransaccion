package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de validación de fraude
 * Ajustado para coincidir con lo que devuelve el servicio validafraude
 */
@Data
@NoArgsConstructor
public class ValidacionFraudeResponse {
    
    private Boolean esFraude;
    private String codigoRegla;
    private String mensaje;
    private String nivelRiesgo;
    
} 