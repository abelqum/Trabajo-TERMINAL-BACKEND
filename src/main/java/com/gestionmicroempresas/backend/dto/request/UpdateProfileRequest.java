package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @JsonProperty("nombre")
        @NotBlank(
                message =
                        "El nombre es obligatorio."
        )
        @Size(
                min = 2,
                max = 150,
                message =
                        "El nombre debe tener entre 2 y 150 caracteres."
        )
        String name

) {
}