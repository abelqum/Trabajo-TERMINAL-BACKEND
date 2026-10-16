package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogItemRequest(

        @JsonProperty("nombre")
        @NotBlank(
                message =
                        "El nombre es obligatorio."
        )
        @Size(
                max = 150,
                message =
                        "El nombre no puede superar los 150 caracteres."
        )
        String name,


        @JsonProperty("enlace")
        @Size(
                max = 500,
                message =
                        "El enlace no puede superar los 500 caracteres."
        )
        String link,


        @JsonProperty("permiteActualizarPrecio")
        boolean allowPriceUpdate

) {
}