package com.banquito.paymentprocessor.procesatransaccion.banquito.dto;

import lombok.Data;

@Data
public class ValidacionMarcaResponseDTO {
    private boolean valida;
    private String marca;
    private String swiftBanco;
    private String mensaje;
} 