package com.banquito.paymentprocessor.procesatransaccion.banquito.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.banquito.paymentprocessor.procesatransaccion.banquito.client")
public class FeignConfig {

}
