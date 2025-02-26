package com.banquito.paymentprocessor.procesatransaccion.banquito.dto;

import lombok.Data;

@Data
public class TransaccionResponseDTO {
    private String estado;
    private String mensaje;
    private String codigoRespuesta;
} 