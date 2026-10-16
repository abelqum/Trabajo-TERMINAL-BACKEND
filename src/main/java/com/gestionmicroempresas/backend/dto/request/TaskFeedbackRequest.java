package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskFeedbackRequest(

        @JsonProperty("comentario")
        @NotBlank(
                message =
                        "La retroalimentación es obligatoria."
        )
        @Size(
                max = 1000,
                message =
                        "La retroalimentación no puede superar los 1000 caracteres."
        )
        String comment

) {
}