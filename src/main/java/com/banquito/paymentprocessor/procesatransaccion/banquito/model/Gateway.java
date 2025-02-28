package com.banquito.paymentprocessor.procesatransaccion.banquito.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GATEWAY")
@Getter
@Setter
@NoArgsConstructor
public class Gateway {
    
    @Id
    @Column(name = "COD_GATEWAY", length = 10, nullable = false)
    private String codGateway;

    @Column(name = "NOMBRE", length = 100, nullable = false)
    private String nombre;
} 