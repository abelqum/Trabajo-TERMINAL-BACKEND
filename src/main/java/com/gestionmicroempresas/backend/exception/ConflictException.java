package com.gestionmicroempresas.backend.exception;

public class ConflictException
        extends RuntimeException {

    public ConflictException(
            String message
    ) {
        super(message);
    }
}