package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TravelsPageResponse(

        List<TravelResponse> content,

        int number,

        int size,

        long totalElements,

        int totalPages,

        @JsonProperty("permisos")
        TravelsPagePermissionsResponse permissions

) {
}