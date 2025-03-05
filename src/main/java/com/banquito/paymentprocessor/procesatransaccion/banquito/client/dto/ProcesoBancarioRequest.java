package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para procesar una transacción con el banco")
public class ProcesoBancarioRequest {

    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codTransaccion;
    
    @Schema(description = "Código único para identificar la transacción", example = "TRANS20240301123456")
    private String codigoUnico;
    
    @Schema(description = "Código del gateway que envía la transacción", example = "PAYPAL")
    private String codigoGtw;

    @Schema(description = "Número de la tarjeta", example = "4532123456789012")
    private String numeroTarjeta;

    @Schema(description = "Código de seguridad de la tarjeta", example = "123")
    private String cvv;

    @Schema(description = "Fecha de caducidad de la tarjeta", example = "12/25")
    private String fechaCaducidad;

    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;

    @Schema(description = "Código de moneda", example = "USD")
    private String codigoMoneda;

    @Schema(description = "Marca de la tarjeta", example = "VISA")
    private String marca;

    @Schema(description = "Estado de la transacción", example = "PEN")
    private String estado;
    
    @Schema(description = "Referencia de la transacción", example = "Compra en línea")
    private String referencia;

    @Schema(description = "País de origen de la transacción", example = "EC")
    private String pais;

    @Schema(description = "Tipo de transacción", example = "COM")
    private String tipo;

    @Schema(description = "Código SWIFT del banco del comercio", example = "BANKECXXXX")
    private String swiftBancoComercio;

    @Schema(description = "Cuenta IBAN del comercio", example = "EC1234567890123456789012")
    private String cuentaIbanComercio;

    @Schema(description = "Código SWIFT del banco emisor de la tarjeta", example = "BANKUS33XXX")
    private String swiftBancoTarjeta;

    @Schema(description = "Datos encriptados de la transacción")
    private String transaccionEncriptada;

    @Schema(description = "Número de meses para el diferido", example = "3")
    private Integer diferido;

    @Schema(description = "Número de cuotas para el diferido", example = "3")
    private Integer cuotas;
} 