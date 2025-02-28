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

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.ValidaFraudeClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.ValidaMarcaClient;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponseDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaResponseDTO;
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
    private ValidaFraudeClient validaFraudeClient;

    @Mock
    private ValidaMarcaClient validaMarcaClient;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private TransaccionService transaccionService;

    private Transaccion transaccion;
    private ValidacionFraudeResponseDTO fraudeResponseExitosa;
    private ValidacionMarcaResponseDTO marcaResponseExitosa;

    @BeforeEach
    void setUp() {
        transaccion = new Transaccion();
        transaccion.setNumeroTarjeta("4111111111111111");
        transaccion.setCvv("123");
        transaccion.setFechaCaducidad("12/25");
        transaccion.setMonto(new BigDecimal("100.00"));
        transaccion.setEstablecimiento("Comercio Test");

        fraudeResponseExitosa = new ValidacionFraudeResponseDTO();
        fraudeResponseExitosa.setEsFraude(false);
        fraudeResponseExitosa.setNivelRiesgo("BAJO");
        fraudeResponseExitosa.setMensaje("No se detectó fraude");

        marcaResponseExitosa = new ValidacionMarcaResponseDTO();
        marcaResponseExitosa.setTarjetaValida(true);
        marcaResponseExitosa.setMensaje("Tarjeta válida");
        marcaResponseExitosa.setSwiftBanco("BANKEC21XXX");
    }

    @Test
    void procesarTransaccion_exitoso() {
        when(validaFraudeClient.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(fraudeResponseExitosa);
        when(validaMarcaClient.validarMarca(any(ValidacionMarcaRequestDTO.class)))
                .thenReturn(marcaResponseExitosa);
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Transaccion resultado = transaccionService.procesarTransaccion(transaccion);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertNotNull(resultado.getCodTransaccion());
        assertEquals("VALIDADA", resultado.getEstado());
        assertEquals("BANKEC21XXX", resultado.getSwiftBanco());
        verify(redisService).saveTransaccion(any(Transaccion.class));
        verify(historialRepository, times(1)).save(any());
    }

    @Test
    void procesarTransaccion_fraudeDetectado() {
        ValidacionFraudeResponseDTO fraudeResponseFraude = new ValidacionFraudeResponseDTO();
        fraudeResponseFraude.setEsFraude(true);
        fraudeResponseFraude.setNivelRiesgo("ALTO");
        fraudeResponseFraude.setMensaje("Transacción sospechosa");

        when(validaFraudeClient.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(fraudeResponseFraude);

        assertThrows(TransaccionRechazadaException.class, () -> {
            transaccionService.procesarTransaccion(transaccion);
        });

        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
        verify(historialRepository, times(1)).save(any());
    }

    @Test
    void procesarTransaccion_tarjetaInvalida() {
        ValidacionMarcaResponseDTO marcaResponseInvalida = new ValidacionMarcaResponseDTO();
        marcaResponseInvalida.setTarjetaValida(false);
        marcaResponseInvalida.setMensaje("Tarjeta expirada");

        when(validaFraudeClient.validarTransaccion(any(ValidacionFraudeRequestDTO.class)))
                .thenReturn(fraudeResponseExitosa);
        when(validaMarcaClient.validarMarca(any(ValidacionMarcaRequestDTO.class)))
                .thenReturn(marcaResponseInvalida);

        assertThrows(TransaccionRechazadaException.class, () -> {
            transaccionService.procesarTransaccion(transaccion);
        });

        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
        verify(historialRepository, times(1)).save(any());
    }

    @Test
    void obtenerTransaccion_encontradaEnRedis() {
        Transaccion transaccionRedis = new Transaccion();
        transaccionRedis.setId(1L);
        transaccionRedis.setCodTransaccion("TRX1234567");

        when(redisService.getTransaccion(1L)).thenReturn(transaccionRedis);

        Transaccion resultado = transaccionService.obtenerTransaccion(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("TRX1234567", resultado.getCodTransaccion());
        verify(transaccionRepository, never()).findById(any());
    }

    @Test
    void obtenerTransaccion_encontradaEnRepo() {
        when(redisService.getTransaccion(1L)).thenReturn(null);
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));

        Transaccion resultado = transaccionService.obtenerTransaccion(1L);

        assertNotNull(resultado);
        assertEquals(transaccion, resultado);
    }

    @Test
    void obtenerTransaccion_noEncontrada() {
        when(redisService.getTransaccion(1L)).thenReturn(null);
        when(transaccionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            transaccionService.obtenerTransaccion(1L);
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
        List<Transaccion> transacciones = Arrays.asList(
                new Transaccion("TRX1234567"),
                new Transaccion("TRX7654321"));

        when(transaccionRepository.findByNumeroTarjeta("4111111111111111")).thenReturn(transacciones);

        List<Transaccion> resultado = transaccionService.findByNumeroTarjeta("4111111111111111");

        assertEquals(2, resultado.size());
    }

    @Test
    void findByEstado_retornaTransaccionesFiltradas() {
        List<Transaccion> transacciones = Arrays.asList(
                new Transaccion("TRX1234567"),
                new Transaccion("TRX7654321"));

        when(transaccionRepository.findByEstado("APROBADA")).thenReturn(transacciones);

        List<Transaccion> resultado = transaccionService.findByEstado("APROBADA");

        assertEquals(2, resultado.size());
    }
} 