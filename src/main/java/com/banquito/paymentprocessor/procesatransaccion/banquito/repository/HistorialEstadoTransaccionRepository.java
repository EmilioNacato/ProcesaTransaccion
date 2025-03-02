package com.banquito.paymentprocessor.procesatransaccion.banquito.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;

@Repository
public interface HistorialEstadoTransaccionRepository extends JpaRepository<HistorialEstadoTransaccion, Long> {
    
    List<HistorialEstadoTransaccion> findByCodTransaccionOrderByFechaEstadoCambioDesc(String codTransaccion);
    
    List<HistorialEstadoTransaccion> findByEstadoOrderByFechaEstadoCambioDesc(String estado);
    
    List<HistorialEstadoTransaccion> findByFechaEstadoCambioBetweenOrderByFechaEstadoCambioDesc(
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin);
} 