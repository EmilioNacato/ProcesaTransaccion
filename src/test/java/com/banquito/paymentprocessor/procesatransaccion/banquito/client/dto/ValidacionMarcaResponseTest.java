package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionMarcaResponseTest {

    @Test
    void testIsValidaConValorExplicito() {
        // Cuando tarjetaValida es true explícitamente
        ValidacionMarcaResponse respuestaValida = new ValidacionMarcaResponse();
        respuestaValida.setTarjetaValida(true);
        respuestaValida.setMensaje("La tarjeta es válida");
        respuestaValida.setSwiftBanco("PICHECU0001");
        
        assertTrue(respuestaValida.isValida());
        
        // Cuando tarjetaValida es false explícitamente
        ValidacionMarcaResponse respuestaInvalida = new ValidacionMarcaResponse();
        respuestaInvalida.setTarjetaValida(false);
        respuestaInvalida.setMensaje("Datos de la tarjeta incorrectos");
        respuestaInvalida.setSwiftBanco(null);
        
        assertFalse(respuestaInvalida.isValida());
    }
    
    @Test
    void testIsValidaConErrorHttp() {
        // Mensaje de error HTTP 404
        ValidacionMarcaResponse respuesta = new ValidacionMarcaResponse();
        respuesta.setTarjetaValida(null);
        respuesta.setMensaje("[404 Not Found] during [POST] to [http://localhost:3000/api/v1/tarjetas/validar]");
        respuesta.setSwiftBanco("N/A");
        
        assertFalse(respuesta.isValida());
        
        // Mensaje de error HTTP 500
        respuesta.setMensaje("[500 Internal Server Error] during [POST]");
        assertFalse(respuesta.isValida());
    }
    
    @Test
    void testIsValidaConMensajeError() {
        // Mensaje con "no encontrada"
        ValidacionMarcaResponse respuesta = new ValidacionMarcaResponse();
        respuesta.setTarjetaValida(null);
        respuesta.setMensaje("Tarjeta no encontrada");
        respuesta.setSwiftBanco("BANCO123");
        
        assertFalse(respuesta.isValida());
        
        // Mensaje con "incorrectos"
        respuesta.setMensaje("Datos de la tarjeta incorrectos");
        assertFalse(respuesta.isValida());
    }
    
    @Test
    void testIsValidaConSwiftBancoInvalido() {
        // SwiftBanco null
        ValidacionMarcaResponse respuesta = new ValidacionMarcaResponse();
        respuesta.setTarjetaValida(null);
        respuesta.setMensaje("La tarjeta es válida");
        respuesta.setSwiftBanco(null);
        
        assertFalse(respuesta.isValida());
        
        // SwiftBanco vacío
        respuesta.setSwiftBanco("");
        assertFalse(respuesta.isValida());
        
        // SwiftBanco "N/A"
        respuesta.setSwiftBanco("N/A");
        assertFalse(respuesta.isValida());
    }
    
    @Test
    void testIsValidaEscenarioCompleto() {
        // Caso válido completo
        ValidacionMarcaResponse respuestaValida = new ValidacionMarcaResponse();
        respuestaValida.setTarjetaValida(null);
        respuestaValida.setMensaje("La tarjeta es válida");
        respuestaValida.setSwiftBanco("PICHECU0001");
        
        assertTrue(respuestaValida.isValida());
    }
} 