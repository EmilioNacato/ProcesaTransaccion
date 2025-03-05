package com.banquito.paymentprocessor.procesatransaccion.banquito.context;

/**
 * Clase utilitaria para mantener datos de contexto de la transacción actual.
 * Se utiliza para pasar datos que no están presentes en el modelo de datos.
 */
public class TransaccionContextHolder {
    
    private static final ThreadLocal<Boolean> diferidoContext = new ThreadLocal<>();
    private static final ThreadLocal<Integer> cuotasContext = new ThreadLocal<>();
    
    private TransaccionContextHolder() {
        // Constructor privado para evitar instanciación
    }
    
    /**
     * Establece el valor de diferido para la transacción actual
     * @param diferido true si la transacción es diferida, false en caso contrario
     */
    public static void setDiferido(Boolean diferido) {
        diferidoContext.set(diferido);
    }
    
    /**
     * Obtiene el valor de diferido para la transacción actual
     * @return true si la transacción es diferida, false en caso contrario, null si no está definido
     */
    public static Boolean getDiferido() {
        return diferidoContext.get();
    }
    
    /**
     * Establece el número de cuotas para la transacción actual
     * @param cuotas número de cuotas
     */
    public static void setCuotas(Integer cuotas) {
        cuotasContext.set(cuotas);
    }
    
    /**
     * Obtiene el número de cuotas para la transacción actual
     * @return número de cuotas, null si no está definido
     */
    public static Integer getCuotas() {
        return cuotasContext.get();
    }
    
    /**
     * Limpia los valores almacenados en el contexto para evitar fugas de memoria
     */
    public static void clear() {
        diferidoContext.remove();
        cuotasContext.remove();
    }
} 