package com.banquito.paymentprocessor.procesatransaccion.banquito.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banquito.paymentprocessor.procesatransaccion.banquito.exception.NotFoundException;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Gateway;
import com.banquito.paymentprocessor.procesatransaccion.banquito.repository.GatewayRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayService {
    
    private final GatewayRepository gatewayRepository;

    @Transactional(readOnly = true)
    public List<Gateway> findAll() {
        log.debug("Buscando todos los gateways");
        return this.gatewayRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Gateway findByCodigo(String codGateway) {
        log.debug("Buscando gateway con código: {}", codGateway);
        return this.gatewayRepository.findById(codGateway)
                .orElseThrow(() -> new NotFoundException("Gateway no encontrado con código: " + codGateway));
    }
    
    @Transactional
    public Gateway create(Gateway gateway) {
        log.debug("Creando nuevo gateway: {}", gateway);
        
        // Verificar si ya existe un gateway con el mismo código
        if (this.gatewayRepository.existsById(gateway.getCodGateway())) {
            throw new IllegalArgumentException("Ya existe un gateway con el código: " + gateway.getCodGateway());
        }
        
        return this.gatewayRepository.save(gateway);
    }
    
    @Transactional
    public Gateway update(String codGateway, Gateway gateway) {
        log.debug("Actualizando gateway con código: {}", codGateway);
        
        // Verificar que el gateway exista
        Gateway existingGateway = this.findByCodigo(codGateway);
        
        // Actualizar solo los campos permitidos
        existingGateway.setNombre(gateway.getNombre());
        
        return this.gatewayRepository.save(existingGateway);
    }
    
    @Transactional
    public void delete(String codGateway) {
        log.debug("Eliminando gateway con código: {}", codGateway);
        
        // Verificar que el gateway exista
        Gateway gateway = this.findByCodigo(codGateway);
        
        this.gatewayRepository.delete(gateway);
    }
    
    /**
     * Verifica si un código de gateway existe en la base de datos
     * @param codGateway Código del gateway a verificar
     * @return true si el gateway existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean verificarCodigoGateway(String codGateway) {
        log.debug("Verificando si existe gateway con código: {}", codGateway);
        if (codGateway == null || codGateway.trim().isEmpty()) {
            log.warn("Se intentó verificar un código de gateway nulo o vacío");
            return false;
        }
        
        return this.gatewayRepository.existsById(codGateway);
    }
} 