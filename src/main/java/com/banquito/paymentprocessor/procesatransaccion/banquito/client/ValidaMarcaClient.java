package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.ValidacionMarcaResponseDTO;

@FeignClient(name = "validamarca", url = "${validamarca.url}")
public interface ValidaMarcaClient {
    @PostMapping("/api/validar-marca")
    ValidacionMarcaResponseDTO validarMarca(@RequestBody ValidacionMarcaRequestDTO request);
} 