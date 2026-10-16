package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

import java.util.List;

public record InventoryReportResponse(

        @JsonProperty("productos")
        List<InventoryProductResponse> products,


        @JsonProperty("valorInventario")
        BigDecimal inventoryValue

) {
}