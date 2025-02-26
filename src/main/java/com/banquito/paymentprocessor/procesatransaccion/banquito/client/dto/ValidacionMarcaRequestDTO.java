package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import lombok.Data;

@Data
public class ValidacionMarcaRequestDTO {
    private String numeroTarjeta;
    private String marca;
    private String cvv;
    private String fechaCaducidad;
} 