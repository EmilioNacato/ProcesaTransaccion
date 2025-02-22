package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaRequest;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaResponse;

@FeignClient(name = "marca-service", url = "${app.marca-service.url}")
public interface MarcaClient {
    
    @PostMapping("/api/v1/validacion")
    ValidacionMarcaResponse validarTarjeta(@RequestBody ValidacionMarcaRequest request);
} 