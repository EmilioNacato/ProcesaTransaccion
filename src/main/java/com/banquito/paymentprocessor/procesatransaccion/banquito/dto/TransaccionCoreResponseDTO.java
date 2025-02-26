package com.banquito.paymentprocessor.procesatransaccion.banquito.dto;

import lombok.Data;

@Data
public class TransaccionCoreResponseDTO {
    private String estado;
    private String codigoRespuesta;
    private String mensaje;
} 