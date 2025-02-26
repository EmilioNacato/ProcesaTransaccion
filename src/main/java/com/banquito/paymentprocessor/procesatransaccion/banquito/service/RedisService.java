package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "transaccion:";
    private static final long EXPIRATION_HOURS = 1;

    public void saveTransaccion(Transaccion transaccion) {
        String key = KEY_PREFIX + transaccion.getId();
        log.info("Guardando transacción en Redis con key: {}", key);
        redisTemplate.opsForValue().set(key, transaccion, EXPIRATION_HOURS, TimeUnit.HOURS);
    }

    public Transaccion getTransaccion(Long id) {
        String key = KEY_PREFIX + id;
        log.info("Buscando transacción en Redis con key: {}", key);
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof Transaccion ? (Transaccion) value : null;
    }
} 