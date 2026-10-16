package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record InventorySummaryResponse(

        @JsonProperty("valorInventario")
        BigDecimal inventoryValue,


        @JsonProperty("productosStockBajo")
        long lowStockProducts,


        @JsonProperty("cantidadProductos")
        long productCount

) {
}