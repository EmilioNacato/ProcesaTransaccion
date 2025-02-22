package com.banquito.paymentprocessor.procesatransaccion.banquito.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HISTORIAL_ESTADO_TRANSACCION")
@Data
@NoArgsConstructor
public class HistorialEstadoTransaccion {
    
    @Id
    @Column(name = "COD_HISTORIAL_ESTADO", length = 10)
    private String codHistorialEstado;
    
    @Column(name = "COD_TRANSACCION", length = 10, nullable = false)
    private String codTransaccion;
    
    @Column(name = "ESTADO", length = 6, nullable = false)
    private String estado;
    
    @Column(name = "FECHA_ESTADO_CAMBIO")
    private LocalDateTime fechaEstadoCambio;
} 