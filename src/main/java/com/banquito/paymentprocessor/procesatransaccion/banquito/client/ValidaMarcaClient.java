package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto.ValidacionMarcaResponseDTO;

@FeignClient(name = "validamarca", url = "${validamarca.url}")
public interface ValidaMarcaClient {
    
    @PostMapping("/api/v1/marca/validar")
    ValidacionMarcaResponseDTO validarMarca(@RequestBody ValidacionMarcaRequestDTO request);
} 