package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FinancialDashboardResponse(

        @JsonProperty("tipoVista")
        String viewType,


        @JsonProperty("usuario")
        UserOptionResponse user,


        @JsonProperty("aniosDisponibles")
        List<Integer> availableYears,


        @JsonProperty("historico")
        List<DashboardAnnualPointResponse> historical,


        @JsonProperty("mensual")
        DashboardMonthlyResponse monthly,


        @JsonProperty("resumenPeriodo")
        DashboardFinancialSummaryResponse periodSummary,


        @JsonProperty("categoriasEgresos")
        DashboardExpenseCategoriesResponse expenseCategories

) {
}