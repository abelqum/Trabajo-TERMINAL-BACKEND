package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import java.util.List;

public record KitRequest(

        @JsonProperty("descripcion")
        @NotBlank(
                message =
                        "El nombre del kit es obligatorio."
        )
        @Size(
                max = 200,
                message =
                        "El nombre del kit no puede superar los 200 caracteres."
        )
        String description,


        @JsonProperty("categoriaId")
        Long categoryId,


        @JsonProperty("almacenId")
        Long warehouseId,


        @JsonProperty("fila")
        @Size(
                max = 50,
                message =
                        "La ubicación no puede superar los 50 caracteres."
        )
        String rowLocation,


        @JsonProperty("precioUnitario")
        @NotNull(
                message =
                        "El precio unitario es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El precio unitario no puede ser negativo."
        )
        @Digits(
                integer = 12,
                fraction = 2,
                message =
                        "El precio unitario debe tener máximo 2 decimales."
        )
        BigDecimal unitPrice,


        @JsonProperty("componentes")
        @NotEmpty(
                message =
                        "Agrega al menos un componente."
        )
        List<@Valid KitComponentRequest> components

) {
}