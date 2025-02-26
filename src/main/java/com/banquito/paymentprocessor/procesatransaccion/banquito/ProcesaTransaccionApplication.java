package com.banquito.paymentprocessor.procesatransaccion.banquito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProcesaTransaccionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcesaTransaccionApplication.class, args);
    }
} 