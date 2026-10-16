package com.gestionmicroempresas.backend.exception;

public class UploadTooLargeException
        extends RuntimeException {

    public UploadTooLargeException(
            String message
    ) {

        super(
                message
        );
    }
}