package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DashboardMonthlyResponse(

        @JsonProperty("anio")
        int year,


        @JsonProperty("datos")
        List<DashboardMonthlyPointResponse> data

) {
}