package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.*;
import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.*;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ProcesaTransaccionService {

    @Autowired
    private ValidaFraudeClient validaFraudeClient;
    
    @Autowired
    private ValidaMarcaClient validaMarcaClient;
    
    @Autowired
    private ProcesaCoresClient procesaCoresClient;
    
    @Autowired
    private TransaccionRepository transaccionRepository;
    
    @Autowired
    private RedisTemplate<String, TransaccionDTO> redisTemplate;

    public TransaccionResponseDTO procesarTransaccion(TransaccionDTO transaccion) {
        TransaccionResponseDTO response = new TransaccionResponseDTO();
        
        try {
            // Validación de fraude
            ValidacionFraudeResponseDTO fraudeResponse = validaFraudeClient.validarTransaccion(
                mapearAValidacionFraudeRequest(transaccion));
                
            if (fraudeResponse.isEsFraude()) {
                return crearRespuestaRechazada("Transacción rechazada por fraude");
            }
            
            // Validación de marca
            ValidacionMarcaResponseDTO marcaResponse = validaMarcaClient.validarMarca(
                mapearAValidacionMarcaRequest(transaccion));
                
            if (!marcaResponse.isValida()) {
                return crearRespuestaRechazada("Marca de tarjeta inválida");
            }
            
            // Guardar en Redis temporalmente
            guardarTransaccionTemporal(transaccion);
            
            // Procesar en Core
            TransaccionCoreResponseDTO coreResponse = procesaCoresClient.procesarTransaccion(
                mapearATransaccionCore(transaccion));
                
            if ("APROBADA".equals(coreResponse.getEstado())) {
                transaccion.setEstado("APROBADA");
                Transaccion entidad = mapearDTOaEntidad(transaccion);
                transaccionRepository.save(entidad);
                return crearRespuestaExitosa(transaccion);
            } else {
                return crearRespuestaRechazada(coreResponse.getMensaje());
            }
            
        } catch (Exception e) {
            log.error("Error al procesar transacción: {}", e.getMessage());
            return crearRespuestaError("Error en procesamiento");
        }
    }

    private void guardarTransaccionTemporal(TransaccionDTO transaccion) {
        String key = "transaccion:temporal:" + transaccion.getId();
        redisTemplate.opsForValue().set(key, transaccion);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    private ValidacionFraudeRequestDTO mapearAValidacionFraudeRequest(TransaccionDTO transaccion) {
        ValidacionFraudeRequestDTO request = new ValidacionFraudeRequestDTO();
        request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        request.setMonto(transaccion.getMonto());
        request.setCodigoComercio(transaccion.getCodigoComercio());
        return request;
    }

    private ValidacionMarcaRequestDTO mapearAValidacionMarcaRequest(TransaccionDTO transaccion) {
        ValidacionMarcaRequestDTO request = new ValidacionMarcaRequestDTO();
        request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        return request;
    }

    private TransaccionCoreDTO mapearATransaccionCore(TransaccionDTO transaccion) {
        TransaccionCoreDTO request = new TransaccionCoreDTO();
        request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        request.setMonto(transaccion.getMonto());
        request.setSwiftBanco(transaccion.getSwiftBanco());
        request.setCodigoComercio(transaccion.getCodigoComercio());
        return request;
    }

    private TransaccionResponseDTO crearRespuestaExitosa(TransaccionDTO transaccion) {
        TransaccionResponseDTO response = new TransaccionResponseDTO();
        response.setEstado("APROBADA");
        response.setCodigoRespuesta("00");
        response.setMensaje("Transacción procesada exitosamente");
        return response;
    }

    private TransaccionResponseDTO crearRespuestaRechazada(String mensaje) {
        TransaccionResponseDTO response = new TransaccionResponseDTO();
        response.setEstado("RECHAZADA");
        response.setCodigoRespuesta("01");
        response.setMensaje(mensaje);
        return response;
    }

    private TransaccionResponseDTO crearRespuestaError(String mensaje) {
        TransaccionResponseDTO response = new TransaccionResponseDTO();
        response.setEstado("ERROR");
        response.setCodigoRespuesta("99");
        response.setMensaje(mensaje);
        return response;
    }

    private Transaccion mapearDTOaEntidad(TransaccionDTO dto) {
        Transaccion entidad = new Transaccion();
        entidad.setNumeroTarjeta(dto.getNumeroTarjeta());
        entidad.setMonto(dto.getMonto());
        entidad.setEstado(dto.getEstado());
        entidad.setSwiftBanco(dto.getSwiftBanco());
        entidad.setCodigoUnico(dto.getId().toString());
        entidad.setFechaTransaccion(LocalDateTime.now());
        return entidad;
    }
} 