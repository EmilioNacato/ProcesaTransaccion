package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Gateway;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.GatewayRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GatewayService {
    
    private final GatewayRepository gatewayRepository;

    @Transactional(readOnly = true)
    public List<Gateway> findAll() {
        return this.gatewayRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Gateway> findById(String codGateway) {
        return this.gatewayRepository.findById(codGateway);
    }

    @Transactional
    public Gateway save(Gateway gateway) {
        return this.gatewayRepository.save(gateway);
    }

    @Transactional
    public void deleteById(String codGateway) {
        this.gatewayRepository.deleteById(codGateway);
    }
} 