package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import java.time.LocalDate;

public record TravelExpenseItemRequest(

        @JsonProperty("categoria")
        @NotBlank(
                message =
                        "La categoría del gasto es obligatoria."
        )
        String category,


        @JsonProperty("descripcion")
        @NotBlank(
                message =
                        "La descripción del gasto es obligatoria."
        )
        @Size(
                max = 300,
                message =
                        "La descripción no puede superar los 300 caracteres."
        )
        String description,


        @JsonProperty("monto")
        @NotNull(
                message =
                        "El monto es obligatorio."
        )
        @DecimalMin(
                value = "0.01",
                message =
                        "El monto debe ser mayor que cero."
        )
        BigDecimal amount,


        @JsonProperty("fecha")
        @NotNull(
                message =
                        "La fecha del gasto es obligatoria."
        )
        LocalDate expenseDate,


        @JsonProperty("comprobanteIndice")
        Integer receiptIndex

) {
}