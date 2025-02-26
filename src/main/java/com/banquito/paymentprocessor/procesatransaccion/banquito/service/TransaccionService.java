package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.BancoClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.FraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;


import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final HistorialEstadoTransaccionRepository historialRepository;
    private final MarcaClient marcaClient;
    private final FraudeClient fraudeClient;
    private final BancoClient bancoClient;
    private final RedisService redisService;

    @Transactional
    public Transaccion procesarTransaccion(Transaccion transaccion) {
        log.info("Procesando transacción: {}", transaccion);
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("PENDIENTE");
        transaccion.setCodTransaccion(UUID.randomUUID().toString().substring(0, 10));
        
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        redisService.saveTransaccion(transaccionGuardada);
        
        log.info("Transacción procesada exitosamente: {}", transaccionGuardada);
        return transaccionGuardada;
    }

    public Transaccion obtenerTransaccion(Long id) {
        log.info("Buscando transacción con ID: {}", id);
        Transaccion transaccionRedis = redisService.getTransaccion(id);
        if (transaccionRedis != null) {
            log.info("Transacción encontrada en Redis: {}", transaccionRedis);
            return transaccionRedis;
        }
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transacción no encontrada con ID: " + id));
    }

    @Transactional
    private void actualizarEstadoTransaccion(Transaccion transaccion, String nuevoEstado, String mensaje) {
        transaccion.setEstado(nuevoEstado);
        transaccionRepository.save(transaccion);
        registrarHistorialEstado(transaccion, nuevoEstado, mensaje);
    }

    private void registrarHistorialEstado(Transaccion transaccion, String estado, String mensaje) {
        HistorialEstadoTransaccion historial = new HistorialEstadoTransaccion();
        historial.setCodHistorialEstado(UUID.randomUUID().toString().substring(0, 10));
        historial.setCodTransaccion(transaccion.getCodTransaccion());
        historial.setEstado(estado);
        historial.setFechaEstadoCambio(LocalDateTime.now());
        historialRepository.save(historial);
        log.info("Estado de transacción actualizado: {} - {}", estado, mensaje);
    }

    public List<Transaccion> findAll() {
        return transaccionRepository.findAll();
    }

    public List<Transaccion> findByNumeroTarjeta(String numeroTarjeta) {
        return transaccionRepository.findByNumeroTarjeta(numeroTarjeta);
    }

    public List<Transaccion> findByEstado(String estado) {
        return transaccionRepository.findByEstado(estado);
    }
} 