package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record KitOperationRequest(

        @JsonProperty("cantidad")
        @NotNull(
                message =
                        "La cantidad es obligatoria."
        )
        @Positive(
                message =
                        "La cantidad debe ser mayor que cero."
        )
        Integer quantity

) {
}