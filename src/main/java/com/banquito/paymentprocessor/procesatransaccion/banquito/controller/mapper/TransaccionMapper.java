package com.banquito.paymentprocessor.procesatransaccion.banquito.controller.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.banquito.paymentprocessor.procesatransaccion.banquito.controller.dto.TransaccionDTO;
import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TransaccionMapper {
    
    @Mapping(source = "codigoUnicoTransaccion", target = "codigoUnico")
    @Mapping(source = "codigoSeguridad", target = "cvv", qualifiedByName = "codigoSeguridadToCvv")
    @Mapping(source = "fechaExpiracion", target = "fechaCaducidad")
    @Mapping(source = "moneda", target = "codigoMoneda")
    @Mapping(source = "swift_banco", target = "swiftBancoComercio")
    @Mapping(source = "cuenta_iban", target = "cuentaIbanComercio")
    @Mapping(source = "transaccion_encriptada", target = "transaccionEncriptada")
    @Mapping(target = "fechaTransaccion", expression = "java(generarFechaActual())")
    Transaccion toEntity(TransaccionDTO dto);
    
    @Mapping(source = "codigoUnico", target = "codigoUnicoTransaccion")
    @Mapping(source = "cvv", target = "codigoSeguridad", qualifiedByName = "cvvToCodigoSeguridad")
    @Mapping(source = "fechaCaducidad", target = "fechaExpiracion")
    @Mapping(source = "codigoMoneda", target = "moneda")
    @Mapping(target = "swift_banco", expression = "java(obtenerSwiftBanco(entity))")
    @Mapping(source = "cuentaIbanComercio", target = "cuenta_iban")
    @Mapping(source = "transaccionEncriptada", target = "transaccion_encriptada")
    TransaccionDTO toDTO(Transaccion entity);
    
    List<TransaccionDTO> toDTOList(List<Transaccion> entities);
    
    List<Transaccion> toEntityList(List<TransaccionDTO> dtos);
    
    @Named("codigoSeguridadToCvv")
    default String codigoSeguridadToCvv(Integer codigoSeguridad) {
        return codigoSeguridad != null ? codigoSeguridad.toString() : null;
    }
    
    @Named("cvvToCodigoSeguridad")
    default Integer cvvToCodigoSeguridad(String cvv) {
        try {
            return cvv != null ? Integer.parseInt(cvv) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    default LocalDateTime generarFechaActual() {
        return LocalDateTime.now();
    }
    
    default String obtenerSwiftBanco(Transaccion entity) {
        // Usar primero swiftBancoComercio, si está disponible
        if (entity.getSwiftBancoComercio() != null && !entity.getSwiftBancoComercio().isEmpty()) {
            return entity.getSwiftBancoComercio();
        }
        // Si no, usar swiftBancoTarjeta
        return entity.getSwiftBancoTarjeta();
    }
} 