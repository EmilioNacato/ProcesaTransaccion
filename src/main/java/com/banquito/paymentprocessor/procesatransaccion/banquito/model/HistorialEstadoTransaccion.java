package com.banquito.paymentprocessor.procesatransaccion.banquito.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "HISTORIAL_ESTADO_TRANSACCION")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class HistorialEstadoTransaccion {

    @Id
    @Column(name = "COD_HISTORIAL_ESTADO", length = 10)
    private String codHistorialEstado;

    @Column(name = "COD_TRANSACCION", length = 10, nullable = false)
    private String codTransaccion;

    @Column(name = "ESTADO", length = 3, nullable = false)
    private String estado;

    @Column(name = "FECHA_ESTADO_CAMBIO", nullable = false)
    private LocalDateTime fechaEstadoCambio;

    public HistorialEstadoTransaccion(String codHistorialEstado) {
        this.codHistorialEstado = codHistorialEstado;
    }

    @Override
    public int hashCode() {
        return codHistorialEstado != null ? codHistorialEstado.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HistorialEstadoTransaccion other = (HistorialEstadoTransaccion) obj;
        return codHistorialEstado != null && codHistorialEstado.equals(other.codHistorialEstado);
    }
} 