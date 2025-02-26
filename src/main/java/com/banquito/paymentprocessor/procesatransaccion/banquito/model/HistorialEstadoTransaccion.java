package com.banquito.paymentprocessor.procesatransaccion.banquito.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "HISTORIAL_ESTADO_TRANSACCION")
public class HistorialEstadoTransaccion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "COD_HISTORIAL_ESTADO", length = 10, nullable = false)
    private String codHistorialEstado;
    
    @Column(name = "COD_TRANSACCION", length = 10, nullable = false)
    private String codTransaccion;
    
    @Column(name = "ESTADO", length = 3, nullable = false)
    private String estado;
    
    @Column(name = "MENSAJE", length = 200)
    private String mensaje;
    
    @Column(name = "FECHA_ESTADO_CAMBIO", nullable = false)
    private LocalDateTime fechaEstadoCambio;
} 