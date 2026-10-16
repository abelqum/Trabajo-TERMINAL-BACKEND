package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DeleteInventoryProductsRequest(

        @JsonProperty("ids")
        @NotEmpty(
                message =
                        "Selecciona al menos un producto."
        )
        List<@NotNull Long> ids

) {
}