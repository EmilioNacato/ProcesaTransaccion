package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;
import org.springframework.dao.DataAccessException;

@Service
@Slf4j
public class RedisService {

    private static final String KEY_PREFIX = "transaccion:";
    private static final String CODE_PREFIX = "transaccion:codigo:";
    
    private final RedisTemplate<String, Transaccion> transaccionRedisTemplate;
    
    @Value("${redis.transaccion.expiration:3600}")
    private long transaccionExpiration;

    public RedisService(RedisTemplate<String, Transaccion> transaccionRedisTemplate) {
        this.transaccionRedisTemplate = transaccionRedisTemplate;
    }

    public void saveTransaccion(Transaccion transaccion) {
        if (transaccion == null || transaccion.getCodTransaccion() == null) {
            log.warn("No se puede guardar una transacción nula o sin código");
            return;
        }
        
        try {
            if (transaccion.getId() != null) {
                String idKey = KEY_PREFIX + transaccion.getId();
                log.info("Guardando transacción en Redis por ID: {}", idKey);
                transaccionRedisTemplate.opsForValue().set(idKey, transaccion);
                transaccionRedisTemplate.expire(idKey, transaccionExpiration, TimeUnit.SECONDS);
            }
            
            String codeKey = CODE_PREFIX + transaccion.getCodTransaccion();
            log.info("Guardando transacción en Redis por código: {}", codeKey);
            transaccionRedisTemplate.opsForValue().set(codeKey, transaccion);
            transaccionRedisTemplate.expire(codeKey, transaccionExpiration, TimeUnit.SECONDS);
            
            log.debug("Transacción guardada con expiración de {} segundos", transaccionExpiration);
        } catch (DataAccessException e) {
            log.error("Error al guardar transacción en Redis: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al guardar transacción en Redis: {}", e.getMessage(), e);
        }
    }

    public void updateTransaccion(Transaccion transaccion) {
        if (transaccion == null || transaccion.getCodTransaccion() == null) {
            log.warn("No se puede actualizar una transacción nula o sin código");
            return;
        }
        
        try {
            saveTransaccion(transaccion);
            log.info("Transacción actualizada en Redis: {}", transaccion.getCodTransaccion());
        } catch (Exception e) {
            log.error("Error al actualizar transacción en Redis: {}", e.getMessage(), e);
        }
    }

    public Transaccion getTransaccion(Long id) {
        if (id == null) {
            log.warn("No se puede buscar una transacción con ID nulo");
            return null;
        }
        
        try {
            String key = KEY_PREFIX + id;
            log.info("Buscando transacción en Redis por ID: {}", key);
            Transaccion transaccion = transaccionRedisTemplate.opsForValue().get(key);
            
            if (transaccion != null) {
                log.info("Transacción encontrada en Redis con ID: {}", id);
            } else {
                log.info("No se encontró transacción en Redis con ID: {}", id);
            }
            
            return transaccion;
        } catch (Exception e) {
            log.error("Error al recuperar transacción de Redis por ID: {}", e.getMessage(), e);
            return null;
        }
    }
    
    public Transaccion getTransaccionByCodigo(String codTransaccion) {
        if (codTransaccion == null || codTransaccion.isEmpty()) {
            log.warn("No se puede buscar una transacción con código nulo o vacío");
            return null;
        }
        
        try {
            String key = CODE_PREFIX + codTransaccion;
            log.info("Buscando transacción en Redis por código: {}", key);
            Transaccion transaccion = transaccionRedisTemplate.opsForValue().get(key);
            
            if (transaccion != null) {
                log.info("Transacción encontrada en Redis con código: {}", codTransaccion);
            } else {
                log.info("No se encontró transacción en Redis con código: {}", codTransaccion);
            }
            
            return transaccion;
        } catch (Exception e) {
            log.error("Error al recuperar transacción de Redis por código: {}", e.getMessage(), e);
            return null;
        }
    }

    public void deleteTransaccion(Long id) {
        if (id == null) {
            return;
        }
        
        try {
            String key = KEY_PREFIX + id;
            log.info("Eliminando transacción de Redis por ID: {}", key);
            transaccionRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error al eliminar transacción de Redis por ID: {}", e.getMessage(), e);
        }
    }
    
    public void deleteTransaccionByCodigo(String codTransaccion) {
        if (codTransaccion == null || codTransaccion.isEmpty()) {
            return;
        }
        
        try {
            String key = CODE_PREFIX + codTransaccion;
            log.info("Eliminando transacción de Redis por código: {}", key);
            transaccionRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error al eliminar transacción de Redis por código: {}", e.getMessage(), e);
        }
    }
} 