package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @JsonProperty("passwordActual")
        @NotBlank(
                message =
                        "La contraseña actual es obligatoria."
        )
        String currentPassword,


        @JsonProperty("nuevaPassword")
        @NotBlank(
                message =
                        "La nueva contraseña es obligatoria."
        )
        @Size(
                min = 8,
                max = 100,
                message =
                        "La nueva contraseña debe tener entre 8 y 100 caracteres."
        )
        String newPassword

) {
}