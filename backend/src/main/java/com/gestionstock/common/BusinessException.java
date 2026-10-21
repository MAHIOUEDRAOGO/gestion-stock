package com.gestionstock.common;

/** Erreur métier (règle de gestion non respectée) renvoyée au client en HTTP 400. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
