package com.banquito.paymentprocessor.procesatransaccion.banquito.exception;

public class TransaccionException extends RuntimeException {
    
    public TransaccionException(String message) {
        super(message);
    }

    public TransaccionException(String message, Throwable cause) {
        super(message, cause);
    }
} 