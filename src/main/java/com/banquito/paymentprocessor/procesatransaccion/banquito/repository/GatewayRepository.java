package com.banquito.paymentprocessor.procesatransaccion.banquito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Gateway;

@Repository
public interface GatewayRepository extends JpaRepository<Gateway, String> {
} 