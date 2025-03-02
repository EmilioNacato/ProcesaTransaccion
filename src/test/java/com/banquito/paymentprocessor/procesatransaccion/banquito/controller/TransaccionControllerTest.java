package com.banquito.paymentprocessor.procesatransaccion.banquito.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.TransaccionRechazadaException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import com.banquito.paymentprocessor.procesatransaccion.banquito.service.TransaccionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransaccionController.class)
public class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransaccionService transaccionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaccion transaccion;
    private List<Transaccion> transacciones;

    @BeforeEach
    void setUp() {
        transaccion = new Transaccion();
        transaccion.setId(1L);
        transaccion.setCodTransaccion("TRX1234567");
        transaccion.setNumeroTarjeta("4111111111111111");
        transaccion.setCvv("123");
        transaccion.setFechaCaducidad("12/25");
        transaccion.setMonto(new BigDecimal("100.00"));
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setEstado("APR");
        transaccion.setSwiftBancoTarjeta("BANKEC21XXX");

        Transaccion transaccion2 = new Transaccion();
        transaccion2.setId(2L);
        transaccion2.setCodTransaccion("TRX7654321");
        transaccion2.setNumeroTarjeta("5111111111111111");
        transaccion2.setEstado("PEN");

        transacciones = Arrays.asList(transaccion, transaccion2);
    }

    @Test
    void obtenerTodasLasTransacciones_retornaLista() throws Exception {
        when(transaccionService.findAll()).thenReturn(transacciones);

        mockMvc.perform(get("/api/v1/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codTransaccion", is("TRX1234567")))
                .andExpect(jsonPath("$[1].codTransaccion", is("TRX7654321")));
    }

    @Test
    void obtenerTransaccionPorCodigo_Existente() throws Exception {
        when(transaccionService.obtenerTransaccionPorCodigo("TRX1234567")).thenReturn(transaccion);

        mockMvc.perform(get("/api/v1/transacciones/TRX1234567")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codTransaccion").value("TRX1234567"));
    }

    @Test
    void obtenerTransaccionPorCodigo_NoExistente() throws Exception {
        when(transaccionService.obtenerTransaccionPorCodigo(anyString()))
                .thenThrow(new NotFoundException("Transacción no encontrada"));

        mockMvc.perform(get("/api/v1/transacciones/NOEXISTE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerTransaccionesPorTarjeta() throws Exception {
        when(transaccionService.findByNumeroTarjeta("4111111111111111")).thenReturn(
                Arrays.asList(transaccion));

        mockMvc.perform(get("/api/v1/transacciones/tarjeta/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroTarjeta", is("4111111111111111")));
    }

    @Test
    void obtenerTransaccionesPorEstado() throws Exception {
        when(transaccionService.findByEstado("APR")).thenReturn(
                Arrays.asList(transaccion));

        mockMvc.perform(get("/api/v1/transacciones/estado/APR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado", is("APR")));
    }

    @Test
    void procesarTransaccion_Exitoso() throws Exception {
        when(transaccionService.procesarTransaccion(any(Transaccion.class))).thenReturn(transaccion);

        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("APR")));
    }

    @Test
    void procesarTransaccion_Rechazada() throws Exception {
        when(transaccionService.procesarTransaccion(any(Transaccion.class)))
                .thenThrow(new TransaccionRechazadaException("Tarjeta rechazada: Fondos insuficientes"));

        mockMvc.perform(post("/api/v1/transacciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccion)))
                .andExpect(status().isUnprocessableEntity());
    }
} 