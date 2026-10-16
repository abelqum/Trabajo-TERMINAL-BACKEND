package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DashboardExpenseCategoriesResponse(

        String desde,

        String hasta,


        @JsonProperty("datos")
        List<DashboardExpenseCategoryResponse> data

) {
}