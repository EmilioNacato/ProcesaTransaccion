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
    @Column(name = "FECHA_CADUCIDAD", length = 5, nullable = false)
    @Schema(description = "Fecha de caducidad de la tarjeta", example = "12/25")
    private String fechaCaducidad;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Column(name = "MONTO", precision = 18, scale = 2, nullable = false)
    @Schema(description = "Monto de la transacción", example = "100.50")
    private BigDecimal monto;
    
    @NotNull
    @Size(max = 100)
    @Column(name = "ESTABLECIMIENTO", length = 100, nullable = false)
    @Schema(description = "Nombre del establecimiento", example = "Supermercado XYZ")
    private String establecimiento;
    
    @NotNull
    @Column(name = "FECHA_TRANSACCION", nullable = false)
    @Schema(description = "Fecha y hora de la transacción", example = "2024-02-26T10:30:00")
    private LocalDateTime fechaTransaccion;
    
    @NotNull
    @Size(max = 3)
    @Column(name = "ESTADO", length = 3, nullable = false)
    @Schema(description = "Estado de la transacción", example = "PEN")
    private String estado;
    
    @Size(max = 11)
    @Column(name = "SWIFT_BANCO", length = 11)
    @Schema(description = "Código SWIFT del banco", example = "BANKEC21XXX")
    private String swiftBanco;
    
    public Transaccion(String codTransaccion) {
        this.codTransaccion = codTransaccion;
    }
} 