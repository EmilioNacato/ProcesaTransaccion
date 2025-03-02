package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.FraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaResponse;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.HistorialEstadoTransaccionRepository;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.TransaccionRepository;

@ExtendWith(MockitoExtension.class)
public class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private HistorialEstadoTransaccionRepository historialRepository;

    @Mock
    private FraudeClient fraudeClient;

    @Mock
    private MarcaClient marcaClient;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private TransaccionService transaccionService;

    private Transaccion transaccion;
    private ValidacionFraudeResponse fraudeResponseValido;
    private ValidacionFraudeResponse fraudeResponseInvalido;
    private ValidacionMarcaResponse marcaResponseValida;
    private ValidacionMarcaResponse marcaResponseInvalida;

    @BeforeEach
    void setUp() {
        transaccion = new Transaccion("TRX1234567");
        transaccion.setNumeroTarjeta("4532123456789012");
        transaccion.setCvv("123");
        transaccion.setFechaCaducidad("12/25");
        transaccion.setMonto(new BigDecimal("100.50"));
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("PEN");
        
        fraudeResponseValido = new ValidacionFraudeResponse();
        fraudeResponseValido.setTransaccionValida(true);
        fraudeResponseValido.setMensaje("Transacción válida");
        
        fraudeResponseInvalido = new ValidacionFraudeResponse();
        fraudeResponseInvalido.setTransaccionValida(false);
        fraudeResponseInvalido.setMensaje("Posible fraude detectado");
        
        marcaResponseValida = new ValidacionMarcaResponse();
        marcaResponseValida.setTarjetaValida(true);
        marcaResponseValida.setMensaje("Tarjeta válida");
        marcaResponseValida.setSwiftBanco("BANKEC21XXX");
        
        marcaResponseInvalida = new ValidacionMarcaResponse();
        marcaResponseInvalida.setTarjetaValida(false);
        marcaResponseInvalida.setMensaje("Fondos insuficientes");
    }

    @Test
    void procesarTransaccion_exitoso() {
        when(fraudeClient.validarTransaccion(any())).thenReturn(fraudeResponseValido);
        when(marcaClient.validarTarjeta(any())).thenReturn(marcaResponseValida);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccion);
        
        Transaccion resultado = transaccionService.procesarTransaccion(transaccion);
        
        assertNotNull(resultado);
        assertEquals("TRX1234567", resultado.getCodTransaccion());
        assertEquals("BANKEC21XXX", resultado.getSwiftBancoTarjeta());
        verify(redisService).saveTransaccion(any(Transaccion.class));
        verify(historialRepository).save(any());
    }
    
    @Test
    void procesarTransaccion_fraudeDetectado() {
        when(fraudeClient.validarTransaccion(any())).thenReturn(fraudeResponseInvalido);
        
        assertThrows(TransaccionRechazadaException.class, () -> {
            transaccionService.procesarTransaccion(transaccion);
        });
        
        verify(redisService, never()).saveTransaccion(any(Transaccion.class));
    }
    
    @Test
    void procesarTransaccion_tarjetaInvalida() {
        when(fraudeClient.validarTransaccion(any())).thenReturn(fraudeResponseValido);
        when(marcaClient.validarTarjeta(any())).thenReturn(marcaResponseInvalida);
        
        assertThrows(TransaccionRechazadaException.class, () -> {
            transaccionService.procesarTransaccion(transaccion);
        });
        
        verify(redisService, never()).saveTransaccion(any(Transaccion.class));
    }
    
    @Test
    void obtenerTransaccionPorCodigo_encontradaEnRedis() {
        when(redisService.getTransaccionByCodigo("TRX1234567")).thenReturn(transaccion);
        
        Transaccion resultado = transaccionService.obtenerTransaccionPorCodigo("TRX1234567");
        
        assertNotNull(resultado);
        assertEquals("TRX1234567", resultado.getCodTransaccion());
        verify(transaccionRepository, never()).findByCodTransaccion(any());
    }
    
    @Test
    void obtenerTransaccionPorCodigo_encontradaEnRepo() {
        when(redisService.getTransaccionByCodigo("TRX1234567")).thenReturn(null);
        when(transaccionRepository.findByCodTransaccion("TRX1234567")).thenReturn(Optional.of(transaccion));
        
        Transaccion resultado = transaccionService.obtenerTransaccionPorCodigo("TRX1234567");
        
        assertNotNull(resultado);
        assertEquals(transaccion, resultado);
    }
    
    @Test
    void obtenerTransaccionPorCodigo_noEncontrada() {
        when(redisService.getTransaccionByCodigo("TRX1234567")).thenReturn(null);
        when(transaccionRepository.findByCodTransaccion("TRX1234567")).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> {
            transaccionService.obtenerTransaccionPorCodigo("TRX1234567");
        });
    }
    
    @Test
    void findAll_retornaListaTransacciones() {
        List<Transaccion> transacciones = Arrays.asList(
                new Transaccion("TRX1234567"),
                new Transaccion("TRX7654321"));
                
        when(transaccionRepository.findAll()).thenReturn(transacciones);
        
        List<Transaccion> resultado = transaccionService.findAll();
        
        assertEquals(2, resultado.size());
        assertEquals("TRX1234567", resultado.get(0).getCodTransaccion());
        assertEquals("TRX7654321", resultado.get(1).getCodTransaccion());
    }
    
    @Test
    void findByNumeroTarjeta_retornaTransaccionesFiltradas() {
        Transaccion t1 = new Transaccion("TRX1234567");
        t1.setNumeroTarjeta("4532123456789012");
        Transaccion t2 = new Transaccion("TRX7654321");
        t2.setNumeroTarjeta("4532123456789012");
        
        List<Transaccion> transacciones = Arrays.asList(t1, t2);
        
        when(transaccionRepository.findByNumeroTarjeta("4532123456789012")).thenReturn(transacciones);
        
        List<Transaccion> resultado = transaccionService.findByNumeroTarjeta("4532123456789012");
        
        assertEquals(2, resultado.size());
        assertEquals("4532123456789012", resultado.get(0).getNumeroTarjeta());
        assertEquals("4532123456789012", resultado.get(1).getNumeroTarjeta());
    }
    
    @Test
    void findByEstado_retornaTransaccionesFiltradas() {
        Transaccion t1 = new Transaccion("TRX1234567");
        t1.setEstado("APR");
        Transaccion t2 = new Transaccion("TRX7654321");
        t2.setEstado("APR");
        
        List<Transaccion> transacciones = Arrays.asList(t1, t2);
        
        when(transaccionRepository.findByEstado("APR")).thenReturn(transacciones);
        
        List<Transaccion> resultado = transaccionService.findByEstado("APR");
        
        assertEquals(2, resultado.size());
        assertEquals("APR", resultado.get(0).getEstado());
        assertEquals("APR", resultado.get(1).getEstado());
    }
} 