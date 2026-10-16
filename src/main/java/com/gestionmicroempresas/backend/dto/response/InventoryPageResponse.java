package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InventoryPageResponse(

        List<InventoryProductResponse> content,

        int number,

        int size,

        long totalElements,

        int totalPages,


        @JsonProperty("resumen")
        InventorySummaryResponse summary

) {
}