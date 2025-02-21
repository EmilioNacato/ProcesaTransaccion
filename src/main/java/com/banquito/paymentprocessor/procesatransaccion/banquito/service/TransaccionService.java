package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    public TransaccionService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    public List<Transaccion> findAll() {
        return this.transaccionRepository.findAll();
    }

    public Transaccion findById(String id) {
        return this.transaccionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id, "Transaccion"));
    }

    public List<Transaccion> findByNumeroTarjeta(String numeroTarjeta) {
        return this.transaccionRepository.findByNumeroTarjeta(numeroTarjeta);
    }

    public List<Transaccion> findByEstado(String estado) {
        return this.transaccionRepository.findByEstado(estado);
    }

    @Transactional
    public Transaccion create(Transaccion transaccion) {
        log.debug("Creando nueva transacción");
        transaccion.setCodTransaccion(UUID.randomUUID().toString().substring(0, 10));
        transaccion.setEstado("PEN");
        transaccion.setFechaTransaccion(LocalDateTime.now());
        return this.transaccionRepository.save(transaccion);
    }

    @Transactional
    public Transaccion update(String id, Transaccion transaccion) {
        log.debug("Actualizando transacción con id: {}", id);
        Transaccion transaccionDB = this.findById(id);
        transaccionDB.setEstado(transaccion.getEstado());
        transaccionDB.setSwiftBanco(transaccion.getSwiftBanco());
        return this.transaccionRepository.save(transaccionDB);
    }

    @Transactional
    public void delete(String id) {
        log.debug("Eliminando transacción con id: {}", id);
        Transaccion transaccion = this.findById(id);
        this.transaccionRepository.delete(transaccion);
    }
} 