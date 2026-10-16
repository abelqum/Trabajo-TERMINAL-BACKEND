package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProblemReportRequest(

        @JsonProperty("pagina")
        @NotBlank(
                message =
                        "La página donde ocurrió el problema es obligatoria."
        )
        @Size(
                max = 300,
                message =
                        "La página no puede exceder 300 caracteres."
        )
        String page,


        @JsonProperty("descripcion")
        @NotBlank(
                message =
                        "Describe el problema que encontraste."
        )
        @Size(
                min = 10,
                max = 2000,
                message =
                        "La descripción debe tener entre 10 y 2000 caracteres."
        )
        String description,


        @JsonProperty("requestId")
        @Size(
                max = 100,
                message =
                        "El identificador de la solicitud no es válido."
        )
        String requestId

) {
}