package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ProcesoBancarioResponse;

@FeignClient(name = "banco-service", url = "${app.banco-service.url}")
public interface BancoClient {
    
    @PostMapping("/api/v1/proceso")
    ProcesoBancarioResponse procesarTransaccion(@RequestBody ProcesoBancarioRequest request);
} 