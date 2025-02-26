package com.banquito.paymentprocessor.procesatransaccion.banquito.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String id, String entity) {
        super("No se encontró " + entity + " con id: " + id);
    }
} 