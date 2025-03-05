package com.banquito.paymentprocessor.procesatransaccion.banquito.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;

@Repository
public interface HistorialEstadoTransaccionRepository extends JpaRepository<HistorialEstadoTransaccion, Long> {
    
    /**
     * Busca el historial de estados por código de transacción ordenado por fecha de cambio descendente
     * @param codTransaccion Código de transacción
     * @return Lista de historial de estados ordenada por fecha descendente
     */
    List<HistorialEstadoTransaccion> findByCodTransaccionOrderByFechaEstadoCambioDesc(String codTransaccion);
    
    /**
     * Busca el historial de estados por código de transacción
     * @param codTransaccion Código de transacción
     * @return Lista de historial de estados
     */
    List<HistorialEstadoTransaccion> findByCodTransaccion(String codTransaccion);
    
    List<HistorialEstadoTransaccion> findByEstadoOrderByFechaEstadoCambioDesc(String estado);
    
    List<HistorialEstadoTransaccion> findByFechaEstadoCambioBetweenOrderByFechaEstadoCambioDesc(
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin);
} 