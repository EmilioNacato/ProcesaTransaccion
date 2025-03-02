package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.HistorialEstadoTransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.HistorialEstadoTransaccion;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface HistorialEstadoTransaccionMapper {
    
    HistorialEstadoTransaccionDTO toDTO(HistorialEstadoTransaccion model);
    
    HistorialEstadoTransaccion toModel(HistorialEstadoTransaccionDTO dto);
    
    List<HistorialEstadoTransaccionDTO> toDTOList(List<HistorialEstadoTransaccion> models);
    
    List<HistorialEstadoTransaccion> toModelList(List<HistorialEstadoTransaccionDTO> dtos);
} 