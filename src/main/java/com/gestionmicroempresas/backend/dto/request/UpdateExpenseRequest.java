package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import java.time.LocalDate;

public record UpdateExpenseRequest(

        @JsonProperty("concepto")
        @NotBlank(
                message =
                        "El concepto es obligatorio."
        )
        @Size(
                max = 150,
                message =
                        "El concepto no puede superar los 150 caracteres."
        )
        String concept,


        @JsonProperty("categoria")
        @NotBlank(
                message =
                        "La categoría es obligatoria."
        )
        String category,


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
        @Digits(
                integer = 12,
                fraction = 2,
                message =
                        "El monto debe tener máximo 2 decimales."
        )
        BigDecimal amount,


        @JsonProperty("fecha")
        @NotNull(
                message =
                        "La fecha es obligatoria."
        )
        LocalDate expenseDate,


        @JsonProperty("descripcion")
        @Size(
                max = 500,
                message =
                        "La descripción no puede superar los 500 caracteres."
        )
        String description

) {
}