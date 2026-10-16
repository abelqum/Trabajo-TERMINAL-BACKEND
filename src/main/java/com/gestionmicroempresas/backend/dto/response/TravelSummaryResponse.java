package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TravelSummaryResponse(

        @JsonProperty("gastadoAprobado")
        BigDecimal approvedSpent,

        @JsonProperty("saldo")
        BigDecimal balance,

        @JsonProperty("gastosPendientes")
        long pendingExpenses,

        @JsonProperty("gastosAprobados")
        long approvedExpenses,

        @JsonProperty("gastosRechazados")
        long rejectedExpenses

) {
}