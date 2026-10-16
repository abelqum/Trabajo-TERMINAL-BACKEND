package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record DashboardFinancialSummaryResponse(

        @JsonProperty("ingresos")
        BigDecimal incomes,


        @JsonProperty("pendienteCobro")
        BigDecimal pendingCollection,


        @JsonProperty("egresosRegistrados")
        BigDecimal registeredExpenses,


        @JsonProperty("viaticos")
        BigDecimal travelExpenses,


        @JsonProperty("egresosTotales")
        BigDecimal totalExpenses,


        @JsonProperty("utilidad")
        BigDecimal profit,


        @JsonProperty("margen")
        BigDecimal margin,


        String desde,

        String hasta

) {
}