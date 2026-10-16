package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

import java.util.List;

public record ExpenseMonthResponse(

        @JsonProperty("periodo")
        String period,

        @JsonProperty("anio")
        int year,

        @JsonProperty("mes")
        int month,

        @JsonProperty("totalRegistrados")
        BigDecimal registeredTotal,

        @JsonProperty("totalViaticos")
        BigDecimal travelTotal,

        BigDecimal total,

        @JsonProperty("cantidadRegistrados")
        long registeredCount,

        @JsonProperty("cantidadViaticos")
        long travelCount,

        @JsonProperty("egresos")
        List<ExpenseResponse> expenses

) {
}