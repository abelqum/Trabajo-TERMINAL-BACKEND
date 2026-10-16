package com.gestionmicroempresas.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(

        @NotBlank(
                message =
                        "La contraseña es obligatoria."
        )
        @Size(
                min = 8,
                max = 100,
                message =
                        "La contraseña debe tener entre 8 y 100 caracteres."
        )
        String password

) {
}