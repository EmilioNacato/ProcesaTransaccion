package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

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
                transaccionRepository.save(transaccion);
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
} 