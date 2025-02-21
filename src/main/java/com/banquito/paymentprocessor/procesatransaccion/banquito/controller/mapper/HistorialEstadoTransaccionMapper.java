package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.HistorialEstadoTransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface HistorialEstadoTransaccionMapper {
    
    HistorialEstadoTransaccionDTO toDTO(HistorialEstadoTransaccion model);
    
    HistorialEstadoTransaccion toModel(HistorialEstadoTransaccionDTO dto);
} 