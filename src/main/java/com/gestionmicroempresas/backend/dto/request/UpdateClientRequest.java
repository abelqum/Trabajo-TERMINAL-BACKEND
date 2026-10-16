package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateClientRequest(

        @JsonProperty("razonSocial")
        @NotBlank(
                message =
                        "La razón social es obligatoria."
        )
        @Size(
                min = 2,
                max = 200,
                message =
                        "La razón social debe tener entre 2 y 200 caracteres."
        )
        String businessName,


        @Size(
                max = 13,
                message =
                        "El RFC no puede exceder 13 caracteres."
        )
        String rfc,


        @JsonProperty("telefonos")
        List<
                @NotBlank(
                        message =
                                "El teléfono no puede estar vacío."
                )
                @Size(
                        max = 30,
                        message =
                                "El teléfono no puede exceder 30 caracteres."
                )
                        String
                > phones,


        @JsonProperty("correos")
        List<
                @NotBlank(
                        message =
                                "El correo no puede estar vacío."
                )
                @Email(
                        message =
                                "Uno de los correos no es válido."
                )
                @Size(
                        max = 200,
                        message =
                                "El correo no puede exceder 200 caracteres."
                )
                        String
                > emails

) {
}