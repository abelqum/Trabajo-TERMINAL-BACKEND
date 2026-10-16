package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TravelBudgetRequest(

        @JsonProperty("alimentos")
        @NotNull(
                message =
                        "El presupuesto de alimentos es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El presupuesto no puede ser negativo."
        )
        @Digits(
                integer = 12,
                fraction = 2,
                message =
                        "El presupuesto debe tener máximo 2 decimales."
        )
        BigDecimal food,


        @JsonProperty("gasolina")
        @NotNull(
                message =
                        "El presupuesto de gasolina es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El presupuesto no puede ser negativo."
        )
        BigDecimal gasoline,


        @JsonProperty("casetas")
        @NotNull(
                message =
                        "El presupuesto de casetas es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El presupuesto no puede ser negativo."
        )
        BigDecimal tolls,


        @JsonProperty("hospedaje")
        @NotNull(
                message =
                        "El presupuesto de hospedaje es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El presupuesto no puede ser negativo."
        )
        BigDecimal lodging,


        @JsonProperty("material")
        @NotNull(
                message =
                        "El presupuesto de material es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El presupuesto no puede ser negativo."
        )
        BigDecimal material,


        @JsonProperty("otros")
        @NotNull(
                message =
                        "El presupuesto de otros es obligatorio."
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "El presupuesto no puede ser negativo."
        )
        BigDecimal other

) {
}