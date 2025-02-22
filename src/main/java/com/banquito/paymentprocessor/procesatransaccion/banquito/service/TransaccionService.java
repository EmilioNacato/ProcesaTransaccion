package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.BancoClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.FraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.*;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final HistorialEstadoTransaccionRepository historialRepository;
    private final MarcaClient marcaClient;
    private final FraudeClient fraudeClient;
    private final BancoClient bancoClient;

    public TransaccionService(TransaccionRepository transaccionRepository,
                            HistorialEstadoTransaccionRepository historialRepository,
                            MarcaClient marcaClient,
                            FraudeClient fraudeClient,
                            BancoClient bancoClient) {
        this.transaccionRepository = transaccionRepository;
        this.historialRepository = historialRepository;
        this.marcaClient = marcaClient;
        this.fraudeClient = fraudeClient;
        this.bancoClient = bancoClient;
    }

    @Transactional
    public Transaccion procesarTransaccion(Transaccion transaccion) {
        log.info("Iniciando procesamiento de transacción");
        
        // 1. Inicializar transacción
        transaccion.setCodTransaccion(UUID.randomUUID().toString().substring(0, 10));
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("PEN"); // Pendiente
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        registrarHistorialEstado(transaccionGuardada, "PEN", "Transacción recibida");
        
        try {
            // 2. Validar con marca
            log.info("Iniciando validación con marca");
            ValidacionMarcaResponse respuestaMarca = marcaClient.validarTarjeta(
                new ValidacionMarcaRequest(transaccion.getNumeroTarjeta(), transaccion.getMonto(), transaccion.getCodTransaccion())
            );
            
            if (!respuestaMarca.getTarjetaValida()) {
                actualizarEstadoTransaccion(transaccionGuardada, "REJ", "Tarjeta inválida: " + respuestaMarca.getMensaje());
                throw new TransaccionRechazadaException("Tarjeta inválida: " + respuestaMarca.getMensaje());
            }
            
            transaccionGuardada.setSwiftBanco(respuestaMarca.getSwiftBanco());
            actualizarEstadoTransaccion(transaccionGuardada, "VAL", "Tarjeta validada con marca");
            
            // 3. Validar fraude
            log.info("Iniciando validación de fraude");
            ValidacionFraudeResponse respuestaFraude = fraudeClient.validarTransaccion(
                new ValidacionFraudeRequest(transaccion.getNumeroTarjeta(), transaccion.getMonto(), 
                                         transaccion.getCodTransaccion(), respuestaMarca.getSwiftBanco())
            );
            
            if (!respuestaFraude.getTransaccionValida()) {
                actualizarEstadoTransaccion(transaccionGuardada, "FRA", "Fraude detectado: " + respuestaFraude.getMensaje());
                throw new TransaccionRechazadaException("Fraude detectado: " + respuestaFraude.getMensaje());
            }
            
            actualizarEstadoTransaccion(transaccionGuardada, "PRO", "Validación de fraude exitosa");
            
            // 4. Procesar con banco
            log.info("Iniciando procesamiento con banco");
            ProcesoBancarioResponse respuestaBanco = bancoClient.procesarTransaccion(
                new ProcesoBancarioRequest(transaccion.getNumeroTarjeta(), transaccion.getMonto(),
                                         transaccion.getCodTransaccion(), transaccion.getSwiftBanco(),
                                         "REF-" + transaccion.getCodTransaccion())
            );
            
            if (!respuestaBanco.getTransaccionExitosa()) {
                actualizarEstadoTransaccion(transaccionGuardada, "REJ", "Rechazada por banco: " + respuestaBanco.getMensaje());
                throw new TransaccionRechazadaException("Transacción rechazada por el banco: " + respuestaBanco.getMensaje());
            }
            
            // 5. Finalizar transacción exitosa
            actualizarEstadoTransaccion(transaccionGuardada, "APR", "Transacción aprobada");
            return transaccionGuardada;
            
        } catch (Exception e) {
            log.error("Error procesando transacción: {}", e.getMessage());
            if ("PEN".equals(transaccionGuardada.getEstado())) {
                actualizarEstadoTransaccion(transaccionGuardada, "ERR", "Error en procesamiento: " + e.getMessage());
            }
            throw e;
        }
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

    public Transaccion findById(String id) {
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id, "Transaccion"));
    }

    public List<Transaccion> findByNumeroTarjeta(String numeroTarjeta) {
        return transaccionRepository.findByNumeroTarjeta(numeroTarjeta);
    }

    public List<Transaccion> findByEstado(String estado) {
        return transaccionRepository.findByEstado(estado);
    }
} 