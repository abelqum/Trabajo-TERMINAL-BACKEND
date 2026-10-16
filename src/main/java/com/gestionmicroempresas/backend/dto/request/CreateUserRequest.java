package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

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
        String name,


        @NotBlank(
                message =
                        "El correo es obligatorio."
        )
        @Email(
                message =
                        "El correo no es válido."
        )
        @Size(
                max = 200,
                message =
                        "El correo es demasiado largo."
        )
        String email,


        @JsonProperty("rol")
        @NotBlank(
                message =
                        "El rol es obligatorio."
        )
        @Pattern(
                regexp =
                        "(?i)^(admin|editor|empleado)$",
                message =
                        "El rol debe ser admin, editor o empleado."
        )
        String role,


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