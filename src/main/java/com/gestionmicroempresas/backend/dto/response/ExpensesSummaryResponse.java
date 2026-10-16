package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ExpensesSummaryResponse(

        BigDecimal total,

        @JsonProperty("totalRegistrados")
        BigDecimal registeredTotal,

        @JsonProperty("totalViaticos")
        BigDecimal travelTotal,

        @JsonProperty("cantidadRegistrados")
        long registeredCount,

        @JsonProperty("cantidadViaticos")
        long travelCount

) {
}