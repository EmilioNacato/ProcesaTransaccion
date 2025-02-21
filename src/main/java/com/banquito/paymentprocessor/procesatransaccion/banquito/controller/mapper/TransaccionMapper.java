package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.TransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransaccionMapper {
    
    TransaccionDTO toDTO(Transaccion model);
    
    Transaccion toModel(TransaccionDTO dto);
} 