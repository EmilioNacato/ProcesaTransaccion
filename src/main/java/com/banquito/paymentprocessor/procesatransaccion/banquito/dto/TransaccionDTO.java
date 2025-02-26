package com.banquito.paymentprocessor.procesatransaccion.banquito.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransaccionDTO {
    private Long id;
    private String numeroTarjeta;
    private BigDecimal monto;
    private String estado;
    private String swiftBanco;
} 