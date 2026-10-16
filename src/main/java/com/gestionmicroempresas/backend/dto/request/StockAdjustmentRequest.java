package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(

        @JsonProperty("tipo")
        @NotBlank(
                message =
                        "El tipo de ajuste es obligatorio."
        )
        String type,


        @JsonProperty("cantidad")
        @NotNull(
                message =
                        "La cantidad es obligatoria."
        )
        @Positive(
                message =
                        "La cantidad debe ser mayor que cero."
        )
        Integer quantity,


        @JsonProperty("motivo")
        @Size(
                max = 500,
                message =
                        "El motivo no puede superar los 500 caracteres."
        )
        String reason

) {
}