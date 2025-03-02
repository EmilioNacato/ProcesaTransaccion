package com.banquito.paymentprocessor.procesatransaccion.banquito.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class AppConfig {
    
    /**
     * Configura un ObjectMapper global para toda la aplicación que:
     * 1. Ignora propiedades desconocidas al deserializar JSON
     * 2. Maneja correctamente los tipos de fecha/hora de Java 8
     * 3. No serializa fechas como timestamps
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Ignorar propiedades desconocidas al deserializar
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Soporte para tipos de fecha/hora de Java 8
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
} 