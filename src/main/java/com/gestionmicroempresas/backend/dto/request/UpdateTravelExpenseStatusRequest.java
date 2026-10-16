package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTravelExpenseStatusRequest(

        @JsonProperty("estatus")
        @NotBlank(
                message =
                        "El estatus es obligatorio."
        )
        String status,


        @JsonProperty("motivoRechazo")
        @Size(
                max = 500,
                message =
                        "El motivo del rechazo no puede superar los 500 caracteres."
        )
        String rejectionReason

) {
}