package com.gestionmicroempresas.backend.storage;

public record ValidatedImage(

        String contentType,

        String extension,

        long size

) {
}