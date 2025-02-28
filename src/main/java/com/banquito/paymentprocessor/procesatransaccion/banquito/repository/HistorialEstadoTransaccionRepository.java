package com.banquito.paymentprocessor.procesatransaccion.banquito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;

import java.util.List;

@Repository
public interface HistorialEstadoTransaccionRepository extends JpaRepository<HistorialEstadoTransaccion, Long> {
    List<HistorialEstadoTransaccion> findByCodTransaccion(String codTransaccion);
    List<HistorialEstadoTransaccion> findByEstado(String estado);
} 