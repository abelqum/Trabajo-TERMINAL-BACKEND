package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record KitComponentRequest(

        @JsonProperty("productoId")
        @NotNull(
                message =
                        "El producto del componente es obligatorio."
        )
        Long productId,


        @JsonProperty("cantidadNecesaria")
        @NotNull(
                message =
                        "La cantidad necesaria es obligatoria."
        )
        @DecimalMin(
                value = "0.01",
                message =
                        "La cantidad necesaria debe ser mayor que cero."
        )
        BigDecimal requiredQuantity

) {
}