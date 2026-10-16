package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record MoveTaskRequest(

        @JsonProperty("prioridad")
        @NotBlank(
                message =
                        "La prioridad es obligatoria."
        )
        String priority,


        @JsonProperty("estado")
        @NotBlank(
                message =
                        "El estado es obligatorio."
        )
        String status

) {
}