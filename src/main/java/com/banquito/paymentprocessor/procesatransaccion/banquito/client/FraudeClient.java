package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionFraudeResponse;

@FeignClient(name = "fraude-service", url = "${app.fraude-service.url}")
public interface FraudeClient {
    
    @PostMapping("/v1/fraude/validar")
    ValidacionFraudeResponse validarTransaccion(@RequestBody ValidacionFraudeRequest request);
} 