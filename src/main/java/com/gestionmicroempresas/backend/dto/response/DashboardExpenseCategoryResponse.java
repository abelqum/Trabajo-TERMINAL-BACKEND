package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record DashboardExpenseCategoryResponse(

        @JsonProperty("categoria")
        String category,


        @JsonProperty("total")
        BigDecimal total

) {
}