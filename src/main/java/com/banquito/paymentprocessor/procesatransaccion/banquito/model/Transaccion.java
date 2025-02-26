package com.banquito.paymentprocessor.procesatransaccion.banquito.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "transaccion")
@Data
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NUMERO_TARJETA", length = 16, nullable = false)
    private String numeroTarjeta;

    @Column(name = "MONTO", precision = 18, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(name = "FECHA_TRANSACCION", nullable = false)
    private LocalDateTime fechaTransaccion;

    @Column(name = "ESTADO", length = 3, nullable = false)
    private String estado;

    @Column(name = "SWIFT_BANCO", length = 11)
    private String swiftBanco;

    public Transaccion(String codTransaccion) {
        this.codTransaccion = codTransaccion;
    }

    @Override
    public int hashCode() {
        return codTransaccion != null ? codTransaccion.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaccion other = (Transaccion) obj;
        return codTransaccion != null && codTransaccion.equals(other.codTransaccion);
    }
} 