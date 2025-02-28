package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.MediaType;

@SpringBootTest
@ActiveProfiles("test")
public class ValidaFraudeClientTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance().build();

    @Autowired
    private ValidaFraudeClient validaFraudeClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("app.fraude-service.url", () -> wireMockServer.baseUrl());
    }

    @BeforeEach
    public void configurarStubs() {
        wireMockServer.resetAll();
    }

    @Test
    public void validarTransaccion_sinFraude() throws Exception {
        ValidacionFraudeResponseDTO responseDTO = new ValidacionFraudeResponseDTO();
        responseDTO.setEsFraude(false);
        responseDTO.setMensaje("No se detectó fraude");
        responseDTO.setNivelRiesgo("BAJO");
        
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/fraude/validar"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseDTO))));

        ValidacionFraudeRequestDTO requestDTO = new ValidacionFraudeRequestDTO();
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setMonto(new BigDecimal("100.00"));
        requestDTO.setCodigoComercio("COM001");
        requestDTO.setCodigoUnico("TRX1234567");
        requestDTO.setTipoTransaccion("COMPRA");

        ValidacionFraudeResponseDTO resultado = validaFraudeClient.validarTransaccion(requestDTO);

        assertNotNull(resultado);
        assertFalse(resultado.isEsFraude());
        assertEquals("BAJO", resultado.getNivelRiesgo());
        assertEquals("No se detectó fraude", resultado.getMensaje());
    }

    @Test
    public void validarTransaccion_conFraude() throws Exception {
        ValidacionFraudeResponseDTO responseDTO = new ValidacionFraudeResponseDTO();
        responseDTO.setEsFraude(true);
        responseDTO.setMensaje("Posible fraude detectado");
        responseDTO.setNivelRiesgo("ALTO");
        responseDTO.setCodigoRegla("FRAUDE-001");
        
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/fraude/validar"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseDTO))));

        ValidacionFraudeRequestDTO requestDTO = new ValidacionFraudeRequestDTO();
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setMonto(new BigDecimal("10000.00"));
        requestDTO.setCodigoComercio("COM001");
        requestDTO.setCodigoUnico("TRX1234567");
        requestDTO.setTipoTransaccion("COMPRA");

        ValidacionFraudeResponseDTO resultado = validaFraudeClient.validarTransaccion(requestDTO);

        assertNotNull(resultado);
        assertTrue(resultado.isEsFraude());
        assertEquals("ALTO", resultado.getNivelRiesgo());
        assertEquals("Posible fraude detectado", resultado.getMensaje());
        assertEquals("FRAUDE-001", resultado.getCodigoRegla());
    }
} 