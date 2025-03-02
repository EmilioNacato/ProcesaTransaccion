package com.banquito.paymentprocessor.procesatransaccion.banquito.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    
    Optional<Transaccion> findByCodTransaccion(String codTransaccion);
    
    List<Transaccion> findByNumeroTarjeta(String numeroTarjeta);
    
    List<Transaccion> findByEstado(String estado);
    
    List<Transaccion> findBySwiftBancoTarjeta(String swiftBancoTarjeta);
    
    List<Transaccion> findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin);
    
    Page<Transaccion> findByFechaTransaccionBetween(
            LocalDateTime fechaInicio, 
            LocalDateTime fechaFin, 
            Pageable pageable);
} 