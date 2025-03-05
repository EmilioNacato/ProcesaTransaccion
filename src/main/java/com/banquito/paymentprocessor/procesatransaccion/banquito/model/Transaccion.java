package com.banquito.paymentprocessor.procesatransaccion.banquito.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.persistence.Transient;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "TRANSACCION")
@Schema(description = "Entidad que representa una transacción de pago")
public class Transaccion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Schema(description = "Identificador único de la transacción", example = "1")
    private Long id;
    
    @NotNull
    @Size(min = 10, max = 10)
    @Column(name = "COD_TRANSACCION", length = 10, nullable = false, unique = true)
    @Schema(description = "Código único de la transacción", example = "TRX1234567")
    private String codTransaccion;
    
    @NotNull
    @Size(max = 64)
    @Column(name = "CODIGO_UNICO", length = 64, nullable = false)
    @Schema(description = "Código único para identificar la transacción", example = "TRANS20240301123456")
    private String codigoUnico;
    
    @NotNull
    @Size(max = 10)
    @Transient
    @Schema(description = "Código del gateway que envía la transacción (solo para validación)", example = "PAYPAL")
    private String codigoGtw;
    
    @NotNull
    @Size(min = 16, max = 16)
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe contener 16 dígitos")
    @Column(name = "NUMERO_TARJETA", length = 16, nullable = false)
    @Schema(description = "Número de la tarjeta", example = "4532123456789012")
    private String numeroTarjeta;
    
    @NotNull
    @Size(min = 3, max = 4)
    @Column(name = "CVV", length = 4, nullable = false)
    @Schema(description = "Código de seguridad de la tarjeta", example = "123")
    private String cvv;
    
    @NotNull
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Formato de fecha inválido (MM/YY)")
    @Column(name = "FECHA_CADUCIDAD_TARJETA", length = 5, nullable = false)
    @Schema(description = "Fecha de caducidad de la tarjeta", example = "12/25")
    private String fechaCaducidad;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Column(name = "MONTO", precision = 20, scale = 2, nullable = false)
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
    
    @Size(max = 3)
    @Column(name = "CODIGO_MONEDA", length = 3)
    @Schema(description = "Código de moneda", example = "USD")
    private String codigoMoneda;
    
    @Size(max = 4)
    @Column(name = "MARCA", length = 4)
    @Schema(description = "Marca de la tarjeta", example = "VISA")
    private String marca;
    
    @NotNull
    @Size(max = 3)
    @Column(name = "ESTADO", length = 3, nullable = false)
    @Schema(description = "Estado de la transacción", example = "PEN")
    private String estado;
    
    @NotNull
    @Column(name = "FECHA_CREACION", nullable = false)
    @Schema(description = "Fecha y hora de la transacción", example = "2024-02-26T10:30:00")
    private LocalDateTime fechaTransaccion;
    
    @Size(max = 50)
    @Column(name = "REFERENCIA", length = 50)
    @Schema(description = "Referencia de la transacción", example = "Compra en línea")
    private String referencia;
    
    @Size(max = 2)
    @Column(name = "PAIS", length = 2)
    @Schema(description = "País de origen de la transacción", example = "EC")
    private String pais;
    
    @Size(max = 3)
    @Column(name = "TIPO", length = 3)
    @Schema(description = "Tipo de transacción", example = "COM")
    private String tipo;
    
    @Size(max = 11)
    @Column(name = "SWIFT_BANCO_COMERCIO", length = 11)
    @Schema(description = "Código SWIFT del banco del comercio", example = "BANKECXXXX")
    private String swiftBancoComercio;
    
    @Size(max = 28)
    @Column(name = "CUENTA_IBAN_COMERCIO", length = 28)
    @Schema(description = "Cuenta IBAN del comercio", example = "EC1234567890123456789012")
    private String cuentaIbanComercio;
    
    @Size(max = 11)
    @Column(name = "SWIFT_BANCO_TARJETA", length = 11)
    @Schema(description = "Código SWIFT del banco emisor de la tarjeta", example = "BANKUS33XXX")
    private String swiftBancoTarjeta;
    
    @Size(max = 1000)
    @Column(name = "TRANSACCION_ENCRIPTADA", length = 1000)
    @Schema(description = "Datos encriptados de la transacción")
    private String transaccionEncriptada;
    
    public Transaccion(String codTransaccion) {
        this.codTransaccion = codTransaccion;
    }
    
    public void setCodigoSeguridad(Integer codigoSeguridad) {
        this.cvv = String.valueOf(codigoSeguridad);
    }
    
    public Integer getCodigoSeguridad() {
        return Integer.valueOf(this.cvv);
    }
} 