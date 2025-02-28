package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponseDTO;

@FeignClient(name = "validafraude", url = "${app.fraude-service.url}")
public interface ValidaFraudeClient {
    
    @PostMapping("/api/v1/fraude/validar")
    ValidacionFraudeResponseDTO validarTransaccion(@RequestBody ValidacionFraudeRequestDTO request);
} 