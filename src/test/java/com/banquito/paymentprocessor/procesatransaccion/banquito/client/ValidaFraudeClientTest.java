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

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponse;
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
    private FraudeClient fraudeClient;

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
        ValidacionFraudeResponse responseDTO = new ValidacionFraudeResponse();
        responseDTO.setTransaccionValida(true);
        responseDTO.setMensaje("No se detectó fraude");
        responseDTO.setCodigoRespuesta("00");
        
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/fraude/validar"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseDTO))));

        ValidacionFraudeRequest requestDTO = new ValidacionFraudeRequest();
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setMonto(new BigDecimal("100.00"));
        requestDTO.setCodTransaccion("TRX1234567");
        requestDTO.setSwiftBanco("BANKEC21");

        ValidacionFraudeResponse resultado = fraudeClient.validarTransaccion(requestDTO);

        assertNotNull(resultado);
        assertTrue(resultado.getTransaccionValida());
        assertEquals("No se detectó fraude", resultado.getMensaje());
        assertEquals("00", resultado.getCodigoRespuesta());
    }

    @Test
    public void validarTransaccion_conFraude() throws Exception {
        ValidacionFraudeResponse responseDTO = new ValidacionFraudeResponse();
        responseDTO.setTransaccionValida(false);
        responseDTO.setMensaje("Posible fraude detectado");
        responseDTO.setCodigoRespuesta("05");
        
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/fraude/validar"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseDTO))));

        ValidacionFraudeRequest requestDTO = new ValidacionFraudeRequest();
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setMonto(new BigDecimal("10000.00"));
        requestDTO.setCodTransaccion("TRX1234567");
        requestDTO.setSwiftBanco("BANKEC21");

        ValidacionFraudeResponse resultado = fraudeClient.validarTransaccion(requestDTO);

        assertNotNull(resultado);
        assertFalse(resultado.getTransaccionValida());
        assertEquals("Posible fraude detectado", resultado.getMensaje());
        assertEquals("05", resultado.getCodigoRespuesta());
    }
} 