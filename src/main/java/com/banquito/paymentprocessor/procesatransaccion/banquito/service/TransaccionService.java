package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.BancoClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.FraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.ValidaFraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.ValidaMarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.*;

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
    private final ValidaFraudeClient validaFraudeClient;
    private final ValidaMarcaClient validaMarcaClient;

    @Transactional
    public Transaccion procesarTransaccion(Transaccion transaccion) {
        log.info("Iniciando procesamiento de transacción");
        
        // Inicializar transacción
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("PENDIENTE");
        transaccion.setCodTransaccion(UUID.randomUUID().toString().substring(0, 10));
        
        try {
            // 1. Validar Fraude
            log.info("Iniciando validación de fraude");
            ValidacionFraudeRequestDTO fraudeRequest = new ValidacionFraudeRequestDTO();
            fraudeRequest.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            fraudeRequest.setMonto(transaccion.getMonto());
            fraudeRequest.setCodigoComercio(transaccion.getEstablecimiento());

            ValidacionFraudeResponseDTO fraudeResponse = validaFraudeClient.validarTransaccion(fraudeRequest);
            
            if (fraudeResponse.isEsFraude()) {
                actualizarEstadoTransaccion(transaccion, "FRAUDE", 
                    "Transacción rechazada por fraude: " + fraudeResponse.getMensaje());
                throw new TransaccionRechazadaException("Fraude detectado: " + fraudeResponse.getMensaje());
            }

            // 2. Validar Marca
            log.info("Iniciando validación de marca");
            ValidacionMarcaRequestDTO marcaRequest = new ValidacionMarcaRequestDTO();
            marcaRequest.setNumeroTarjeta(transaccion.getNumeroTarjeta());
            marcaRequest.setMarca(obtenerMarcaTarjeta(transaccion.getNumeroTarjeta()));
            marcaRequest.setCvv(transaccion.getCvv());
            marcaRequest.setFechaCaducidad(transaccion.getFechaCaducidad());

            ValidacionMarcaResponseDTO marcaResponse = validaMarcaClient.validarMarca(marcaRequest);
            
            if (!marcaResponse.isTarjetaValida()) {
                actualizarEstadoTransaccion(transaccion, "RECHAZADA", 
                    "Tarjeta inválida: " + marcaResponse.getMensaje());
                throw new TransaccionRechazadaException("Tarjeta inválida: " + marcaResponse.getMensaje());
            }

            // Actualizar SWIFT del banco
            transaccion.setSwiftBanco(marcaResponse.getSwiftBanco());
            
            // 3. Guardar en Redis temporalmente
            redisService.saveTransaccion(transaccion);
            
            // 4. Guardar en PostgreSQL
            Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
            actualizarEstadoTransaccion(transaccionGuardada, "VALIDADA", 
                "Transacción validada correctamente");
            
            return transaccionGuardada;
            
        } catch (TransaccionRechazadaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error procesando transacción: {}", e.getMessage());
            actualizarEstadoTransaccion(transaccion, "ERROR", 
                "Error en procesamiento: " + e.getMessage());
            throw new RuntimeException("Error procesando transacción", e);
        }
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
    private void actualizarEstadoTransaccion(Transaccion transaccion, String estado, String mensaje) {
        transaccion.setEstado(estado);
        transaccionRepository.save(transaccion);
        registrarHistorialEstado(transaccion, estado, mensaje);
        log.info("Estado de transacción actualizado a: {} - {}", estado, mensaje);
    }

    private void registrarHistorialEstado(Transaccion transaccion, String estado, String mensaje) {
        HistorialEstadoTransaccion historial = new HistorialEstadoTransaccion();
        historial.setCodHistorialEstado(UUID.randomUUID().toString().substring(0, 10));
        historial.setCodTransaccion(transaccion.getCodTransaccion());
        historial.setEstado(estado);
        historial.setMensaje(mensaje);
        historial.setFechaEstadoCambio(LocalDateTime.now());
        historialRepository.save(historial);
    }

    private String obtenerMarcaTarjeta(String numeroTarjeta) {
        if (numeroTarjeta.startsWith("4")) return "VISA";
        if (numeroTarjeta.startsWith("5")) return "MASTERCARD";
        if (numeroTarjeta.startsWith("34") || numeroTarjeta.startsWith("37")) return "AMEX";
        return "DESCONOCIDA";
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