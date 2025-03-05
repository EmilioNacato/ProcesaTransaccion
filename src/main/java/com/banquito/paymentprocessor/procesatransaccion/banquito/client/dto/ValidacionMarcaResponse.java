package com.banquito.paymentprocessor.procesatransaccion.banquito.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta de la validación de una tarjeta con su marca")
public class ValidacionMarcaResponse {
    
    @JsonProperty("esValida")
    @Schema(description = "Indica si la tarjeta es válida", example = "true")
    private Boolean tarjetaValida;
    
    @Schema(description = "Código SWIFT del banco", example = "BANKEC21")
    private String swiftBanco;
    
    @Schema(description = "Mensaje informativo del proceso", example = "Tarjeta validada correctamente")
    private String mensaje;
    
    /**
     * Determina si la tarjeta es válida basándose en múltiples campos
     * @return true si la tarjeta es válida, false en caso contrario
     */
    public boolean isValida() {
        // Si tenemos un valor explícito de tarjetaValida, lo usamos
        if (tarjetaValida != null) {
            return tarjetaValida;
        }
        
        // Verificar si el mensaje contiene errores HTTP o mensajes de error específicos
        if (mensaje != null) {
            String mensajeLower = mensaje.toLowerCase();
            
            // Verificar si contiene códigos de error HTTP
            if (mensajeLower.contains("[404") || 
                mensajeLower.contains("[400") || 
                mensajeLower.contains("[500") || 
                mensajeLower.contains("error") || 
                mensajeLower.contains("exception")) {
                return false;
            }
            
            // Verificar si contiene mensajes específicos de error
            if (mensajeLower.contains("incorrectos") || 
                mensajeLower.contains("inválida") || 
                mensajeLower.contains("invalida") || 
                mensajeLower.contains("rechaza") || 
                mensajeLower.contains("no encontrada") || 
                mensajeLower.contains("no válida") || 
                mensajeLower.contains("not found")) {
                return false;
            }
        }
        
        // Verificar si el swiftBanco es válido (no puede ser N/A o vacío)
        if (swiftBanco == null || swiftBanco.isEmpty() || "N/A".equals(swiftBanco)) {
            return false;
        }
        
        // Si llegamos aquí y no hay indicadores negativos, consideramos válida
        return true;
    }
    
    @Override
    public String toString() {
        return "ValidacionMarcaResponse [tarjetaValida=" + tarjetaValida + 
               ", mensaje=" + mensaje + 
               ", swiftBanco=" + swiftBanco + 
               ", esValida()=" + isValida() + "]";
    }
} 