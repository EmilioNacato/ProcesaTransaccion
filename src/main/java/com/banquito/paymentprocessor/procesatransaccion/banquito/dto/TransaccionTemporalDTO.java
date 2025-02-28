package com.banquito.paymentprocessor.procesatransaccion.banquito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransaccionTemporalDTO {
    
    private Long id;
    private String codTransaccion;
    private String numeroTarjeta;
    private String cvv;
    private String fechaCaducidad;
    private BigDecimal monto;
    private String establecimiento;
    private LocalDateTime fechaTransaccion;
    private String estado;
    private String swiftBanco;
    
    public static TransaccionTemporalDTO fromTransaccion(com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion transaccion) {
        TransaccionTemporalDTO dto = new TransaccionTemporalDTO();
        dto.setId(transaccion.getId());
        dto.setCodTransaccion(transaccion.getCodTransaccion());
        dto.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        dto.setCvv(transaccion.getCvv());
        dto.setFechaCaducidad(transaccion.getFechaCaducidad());
        dto.setMonto(transaccion.getMonto());
        dto.setEstablecimiento(transaccion.getEstablecimiento());
        dto.setFechaTransaccion(transaccion.getFechaTransaccion());
        dto.setEstado(transaccion.getEstado());
        dto.setSwiftBanco(transaccion.getSwiftBanco());
        return dto;
    }
} 