package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record TaskEvidenceResponse(

        Long id,

        String url,


        @JsonProperty("nombreOriginal")
        String originalName,


        @JsonProperty("mimeType")
        String mimeType,


        @JsonProperty("tamanoBytes")
        long sizeBytes,


        @JsonProperty("autor")
        UserOptionResponse author,


        @JsonProperty("puedeEliminar")
        boolean canDelete,


        @JsonProperty("creadoEn")
        OffsetDateTime createdAt

) {
}