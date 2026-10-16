package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

import java.util.List;

public record TravelResponse(

        Long id,

        @JsonProperty("nombre")
        String name,

        @JsonProperty("destino")
        String destination,

        @JsonProperty("fechaInicio")
        LocalDate startDate,

        @JsonProperty("fechaFin")
        LocalDate endDate,

        @JsonProperty("dias")
        long days,

        @JsonProperty("estatus")
        String status,

        @JsonProperty("presupuesto")
        TravelBudgetResponse budget,

        @JsonProperty("usuarios")
        List<UserOptionResponse> users,

        @JsonProperty("resumen")
        TravelSummaryResponse summary,

        @JsonProperty("gastos")
        List<TravelExpenseResponse> expenses,

        @JsonProperty("permisos")
        TravelPermissionsResponse permissions

) {
}