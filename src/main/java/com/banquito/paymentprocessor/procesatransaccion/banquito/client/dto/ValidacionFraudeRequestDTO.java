package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ValidacionFraudeRequestDTO {
    private String numeroTarjeta;
    private BigDecimal monto;
    private String codigoComercio;
} 