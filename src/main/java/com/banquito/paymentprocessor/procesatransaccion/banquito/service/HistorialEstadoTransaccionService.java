package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistorialEstadoTransaccionService {

    private final HistorialEstadoTransaccionRepository repository;

    @Transactional(readOnly = true)
    public List<HistorialEstadoTransaccion> findAll() {
        return this.repository.findAll();
    }

    @Transactional(readOnly = true)
    public HistorialEstadoTransaccion findById(Long id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Historial no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoTransaccion> findByCodTransaccion(String codTransaccion) {
        log.info("Buscando historial para la transacción: {}", codTransaccion);
        return this.repository.findByCodTransaccionOrderByFechaEstadoCambioDesc(codTransaccion);
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoTransaccion> findByEstado(String estado) {
        log.info("Buscando historial para el estado: {}", estado);
        return this.repository.findByEstadoOrderByFechaEstadoCambioDesc(estado);
    }
    
    @Transactional(readOnly = true)
    public List<HistorialEstadoTransaccion> findByFechaEstadoCambioBetween(LocalDateTime desde, LocalDateTime hasta) {
        log.info("Buscando historial entre {} y {}", desde, hasta);
        return this.repository.findByFechaEstadoCambioBetweenOrderByFechaEstadoCambioDesc(desde, hasta);
    }

    @Transactional
    public HistorialEstadoTransaccion create(HistorialEstadoTransaccion historial) {
        log.info("Creando nuevo historial de estado para transacción: {}", historial.getCodTransaccion());
        
        // Generamos un código único si no viene especificado
        if (historial.getCodHistorialEstado() == null || historial.getCodHistorialEstado().isEmpty()) {
            historial.setCodHistorialEstado(UUID.randomUUID().toString().substring(0, 10));
        }
        
        // Establecemos la fecha actual si no viene especificada
        if (historial.getFechaEstadoCambio() == null) {
            historial.setFechaEstadoCambio(LocalDateTime.now());
        }
        
        return this.repository.save(historial);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Eliminando historial con id: {}", id);
        
        HistorialEstadoTransaccion historial = this.findById(id);
        this.repository.delete(historial);
    }
} 