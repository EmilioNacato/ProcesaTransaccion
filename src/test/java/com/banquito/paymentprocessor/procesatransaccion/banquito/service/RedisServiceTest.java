package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.TransaccionTemporalDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Transaccion> redisTemplate;

    @Mock
    private RedisTemplate<String, TransaccionTemporalDTO> transaccionTemporalRedisTemplate;

    @Mock
    private ValueOperations<String, Transaccion> valueOperations;

    @Mock
    private ValueOperations<String, TransaccionTemporalDTO> valueOperationsDTO;

    @InjectMocks
    private RedisService redisService;

    private Transaccion transaccion;
    private static final String REDIS_KEY = "transaccion:temporal:TRX1234567";

    @BeforeEach
    void setUp() {
        transaccion = new Transaccion();
        transaccion.setId(1L);
        transaccion.setCodTransaccion("TRX1234567");
        transaccion.setNumeroTarjeta("4111111111111111");
        transaccion.setCvv("123");
        transaccion.setFechaCaducidad("12/25");
        transaccion.setMonto(new BigDecimal("100.00"));
        transaccion.setEstablecimiento("Comercio Test");
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("APR");
        transaccion.setSwiftBanco("BANKEC21XXX");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(transaccionTemporalRedisTemplate.opsForValue()).thenReturn(valueOperationsDTO);
        
        ReflectionTestUtils.setField(redisService, "transaccionExpiration", 3600L);
    }

    @Test
    void saveTransaccion_guardaDatosEnRedis() {
        redisService.saveTransaccion(transaccion);

        verify(valueOperations).set(eq(REDIS_KEY), eq(transaccion));
        verify(redisTemplate).expire(eq(REDIS_KEY), eq(3600L), eq(TimeUnit.SECONDS));
        
        verify(valueOperationsDTO).set(eq(REDIS_KEY), any(TransaccionTemporalDTO.class));
        verify(transaccionTemporalRedisTemplate).expire(eq(REDIS_KEY), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void getTransaccion_existente_retornaTransaccion() {
        when(valueOperations.get(REDIS_KEY)).thenReturn(transaccion);
        
        Transaccion resultado = redisService.getTransaccion("TRX1234567");
        
        assertNotNull(resultado);
        assertEquals(transaccion, resultado);
        verify(valueOperations).get(REDIS_KEY);
    }

    @Test
    void getTransaccion_noExistente_retornaNull() {
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        
        Transaccion resultado = redisService.getTransaccion("TRX1234567");
        
        assertNull(resultado);
        verify(valueOperations).get(REDIS_KEY);
    }

    @Test
    void deleteTransaccion_eliminaDatosDeRedis() {
        redisService.deleteTransaccion("TRX1234567");
        
        verify(redisTemplate).delete(REDIS_KEY);
        verify(transaccionTemporalRedisTemplate).delete(REDIS_KEY);
    }
} 