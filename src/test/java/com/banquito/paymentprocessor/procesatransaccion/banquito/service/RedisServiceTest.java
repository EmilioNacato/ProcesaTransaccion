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

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisService redisService;

    private Transaccion transaccion;
    private static final String KEY_PREFIX = "transaccion:";
    private static final String CODE_PREFIX = "transaccion:codigo:";

    @BeforeEach
    void setUp() {
        transaccion = new Transaccion("TRX1234567");
        transaccion.setId(1L);
        transaccion.setNumeroTarjeta("4111111111111111");
        transaccion.setCvv("123");
        transaccion.setFechaCaducidad("12/25");
        transaccion.setMonto(new BigDecimal("100.00"));
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("APR");
        transaccion.setSwiftBancoTarjeta("BANKEC21XXX");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        ReflectionTestUtils.setField(redisService, "transaccionExpiration", 3600L);
    }

    @Test
    void saveTransaccion_guardaDatosEnRedis() {
        redisService.saveTransaccion(transaccion);

        // Verifica que se guarda por ID
        verify(valueOperations).set(eq(KEY_PREFIX + transaccion.getId()), eq(transaccion));
        verify(redisTemplate).expire(eq(KEY_PREFIX + transaccion.getId()), eq(3600L), eq(TimeUnit.SECONDS));
        
        // Verifica que se guarda por Código
        verify(valueOperations).set(eq(CODE_PREFIX + transaccion.getCodTransaccion()), eq(transaccion));
        verify(redisTemplate).expire(eq(CODE_PREFIX + transaccion.getCodTransaccion()), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void getTransaccion_existente_retornaTransaccion() {
        when(valueOperations.get(KEY_PREFIX + "1")).thenReturn(transaccion);
        
        Transaccion resultado = redisService.getTransaccion(1L);
        
        assertNotNull(resultado);
        assertEquals(transaccion, resultado);
        verify(valueOperations).get(KEY_PREFIX + "1");
    }

    @Test
    void getTransaccionByCodigo_existente_retornaTransaccion() {
        when(valueOperations.get(CODE_PREFIX + "TRX1234567")).thenReturn(transaccion);
        
        Transaccion resultado = redisService.getTransaccionByCodigo("TRX1234567");
        
        assertNotNull(resultado);
        assertEquals(transaccion, resultado);
        verify(valueOperations).get(CODE_PREFIX + "TRX1234567");
    }

    @Test
    void getTransaccion_noExistente_retornaNull() {
        when(valueOperations.get(KEY_PREFIX + "99")).thenReturn(null);
        
        Transaccion resultado = redisService.getTransaccion(99L);
        
        assertNull(resultado);
        verify(valueOperations).get(KEY_PREFIX + "99");
    }

    @Test
    void getTransaccionByCodigo_noExistente_retornaNull() {
        when(valueOperations.get(CODE_PREFIX + "NOEXISTE")).thenReturn(null);
        
        Transaccion resultado = redisService.getTransaccionByCodigo("NOEXISTE");
        
        assertNull(resultado);
        verify(valueOperations).get(CODE_PREFIX + "NOEXISTE");
    }

    @Test
    void deleteTransaccion_eliminaDatosDeRedis() {
        redisService.deleteTransaccion(1L);
        
        verify(redisTemplate).delete(KEY_PREFIX + "1");
    }
    
    @Test
    void deleteTransaccionByCodigo_eliminaDatosDeRedis() {
        redisService.deleteTransaccionByCodigo("TRX1234567");
        
        verify(redisTemplate).delete(CODE_PREFIX + "TRX1234567");
    }
} 