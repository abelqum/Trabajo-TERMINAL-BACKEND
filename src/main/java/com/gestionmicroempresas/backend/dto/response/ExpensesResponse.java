package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExpensesResponse(

        @JsonProperty("resumen")
        ExpensesSummaryResponse summary,

        @JsonProperty("meses")
        List<ExpenseMonthResponse> months

) {
}