package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class HistorialEstadoTransaccionService {

    private final HistorialEstadoTransaccionRepository historialRepository;

    public HistorialEstadoTransaccionService(HistorialEstadoTransaccionRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public List<HistorialEstadoTransaccion> findAll() {
        return this.historialRepository.findAll();
    }

    public HistorialEstadoTransaccion findById(Long id) {
        return this.historialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), "HistorialEstadoTransaccion"));
    }

    public List<HistorialEstadoTransaccion> findByCodTransaccion(String codTransaccion) {
        return this.historialRepository.findByCodTransaccion(codTransaccion);
    }

    public List<HistorialEstadoTransaccion> findByEstado(String estado) {
        return this.historialRepository.findByEstado(estado);
    }

    @Transactional
    public HistorialEstadoTransaccion create(HistorialEstadoTransaccion historial) {
        log.debug("Creando nuevo historial de estado");
        historial.setCodHistorialEstado(UUID.randomUUID().toString().substring(0, 10));
        historial.setFechaEstadoCambio(LocalDateTime.now());
        return this.historialRepository.save(historial);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Eliminando historial de estado con id: {}", id);
        HistorialEstadoTransaccion historial = this.findById(id);
        this.historialRepository.delete(historial);
    }
} 