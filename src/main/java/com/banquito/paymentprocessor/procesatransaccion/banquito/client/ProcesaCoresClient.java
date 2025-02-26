package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "procesacores", url = "${procesacores.url}")
public interface ProcesaCoresClient {
    @PostMapping("/api/procesar")
    TransaccionCoreResponseDTO procesarTransaccion(@RequestBody TransaccionCoreDTO request);
} 