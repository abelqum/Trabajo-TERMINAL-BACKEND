package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

import java.util.List;

public record UpdateTaskRequest(

        @JsonProperty("titulo")
        @NotBlank(
                message =
                        "El título es obligatorio."
        )
        @Size(
                max = 150,
                message =
                        "El título no puede superar los 150 caracteres."
        )
        String title,


        @JsonProperty("descripcion")
        @Size(
                max = 1000,
                message =
                        "La descripción no puede superar los 1000 caracteres."
        )
        String description,


        @JsonProperty("fechaLimite")
        OffsetDateTime dueAt,


        @JsonProperty("prioridad")
        @NotBlank(
                message =
                        "La prioridad es obligatoria."
        )
        String priority,


        @JsonProperty("asignadosIds")
        @NotEmpty(
                message =
                        "Selecciona al menos una persona."
        )
        List<@NotNull Long> assigneeIds

) {
}