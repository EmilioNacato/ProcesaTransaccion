package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.GatewayDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Gateway;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface GatewayMapper {
    
    /**
     * Convierte una entidad Gateway a DTO
     */
    GatewayDTO toDTO(Gateway model);
    
    /**
     * Convierte un DTO a entidad Gateway
     */
    Gateway toModel(GatewayDTO dto);
    
    /**
     * Convierte una lista de entidades Gateway a lista de DTOs
     */
    List<GatewayDTO> toDTOList(List<Gateway> models);
    
    /**
     * Convierte una lista de DTOs a lista de entidades Gateway
     */
    List<Gateway> toModelList(List<GatewayDTO> dtos);
} 