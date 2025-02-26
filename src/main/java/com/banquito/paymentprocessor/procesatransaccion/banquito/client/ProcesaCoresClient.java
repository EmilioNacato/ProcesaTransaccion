package com.banquito.paymentprocessor.procesatransaccion.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.dto.TransaccionCoreResponseDTO;

@FeignClient(name = "procesacores", url = "${procesacores.url}")
public interface ProcesaCoresClient {
    @PostMapping("/api/procesar")
    TransaccionCoreResponseDTO procesarTransaccion(@RequestBody TransaccionCoreDTO request);
} 