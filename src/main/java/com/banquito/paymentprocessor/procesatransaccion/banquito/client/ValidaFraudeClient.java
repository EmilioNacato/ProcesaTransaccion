package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.ValidacionFraudeRequestDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.ValidacionFraudeResponseDTO;

@FeignClient(name = "validafraude", url = "${validafraude.url}")
public interface ValidaFraudeClient {
    @PostMapping("/api/validar-fraude")
    ValidacionFraudeResponseDTO validarTransaccion(@RequestBody ValidacionFraudeRequestDTO request);
} 