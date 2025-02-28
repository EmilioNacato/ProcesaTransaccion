package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    private static final String KEY_PREFIX = "transaccion:temporal:";
    private final RedisTemplate<String, Transaccion> redisTemplate;
    
    @Value("${redis.transaccion.expiration:3600}")
    private long transaccionExpiration; // tiempo en segundos, por defecto 1 hora

    public RedisService(RedisTemplate<String, Transaccion> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveTransaccion(Transaccion transaccion) {
        String key = KEY_PREFIX + transaccion.getCodTransaccion();
        log.info("Guardando transacción temporal en Redis: {}", key);
        redisTemplate.opsForValue().set(key, transaccion);
        redisTemplate.expire(key, transaccionExpiration, TimeUnit.SECONDS);
        log.debug("Transacción guardada con expiración de {} segundos", transaccionExpiration);
    }

    public Transaccion getTransaccion(String codTransaccion) {
        String key = KEY_PREFIX + codTransaccion;
        log.info("Buscando transacción temporal en Redis: {}", key);
        return redisTemplate.opsForValue().get(key);
    }
    
    public Transaccion getTransaccion(Long id) {
        // Mantener compatibilidad con el método anterior
        log.info("Buscando transacción por ID en Redis (obsoleto): {}", id);
        // Este método necesitaría buscar en todas las transacciones para encontrar por ID
        // Es recomendable migrar a búsqueda por codTransaccion
        return null;
    }

    public void deleteTransaccion(String codTransaccion) {
        String key = KEY_PREFIX + codTransaccion;
        log.info("Eliminando transacción temporal de Redis: {}", key);
        redisTemplate.delete(key);
    }
    
    public void deleteTransaccion(Long id) {
        // Mantener compatibilidad con el método anterior
        log.info("Eliminando transacción por ID en Redis (obsoleto): {}", id);
        // Este método necesitaría buscar en todas las transacciones para encontrar por ID
        // Es recomendable migrar a eliminación por codTransaccion
    }
} 