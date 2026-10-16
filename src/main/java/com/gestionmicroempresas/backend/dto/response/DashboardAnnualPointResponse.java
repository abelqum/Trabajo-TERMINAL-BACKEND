package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record DashboardAnnualPointResponse(

        @JsonProperty("anio")
        int year,


        @JsonProperty("ingresos")
        BigDecimal incomes,


        @JsonProperty("egresos")
        BigDecimal expenses,


        @JsonProperty("utilidad")
        BigDecimal profit

) {
}