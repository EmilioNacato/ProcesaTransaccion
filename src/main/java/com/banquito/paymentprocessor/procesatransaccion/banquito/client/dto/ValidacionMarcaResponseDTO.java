package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import lombok.Data;

@Data
public class ValidacionMarcaResponseDTO {
    private boolean tarjetaValida;
    private String marca;
    private String swiftBanco;
    private String mensaje;
} 