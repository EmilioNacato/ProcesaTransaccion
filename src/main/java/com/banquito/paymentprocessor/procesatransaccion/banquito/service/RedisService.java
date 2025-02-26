package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    private static final String KEY_PREFIX = "transaccion:temporal:";
    private final RedisTemplate<String, Transaccion> redisTemplate;

    public RedisService(RedisTemplate<String, Transaccion> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveTransaccion(Transaccion transaccion) {
        String key = KEY_PREFIX + transaccion.getId();
        log.info("Guardando transacción en Redis: {}", key);
        redisTemplate.opsForValue().set(key, transaccion);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public Transaccion getTransaccion(Long id) {
        String key = KEY_PREFIX + id;
        log.info("Buscando transacción en Redis: {}", key);
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteTransaccion(Long id) {
        String key = KEY_PREFIX + id;
        log.info("Eliminando transacción de Redis: {}", key);
        redisTemplate.delete(key);
    }
} 