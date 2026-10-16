package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;

public record CatalogItemResponse(

        Long id,


        @JsonProperty("nombre")
        String name,


        @JsonProperty("enlace")
        String link,


        @JsonProperty("permiteActualizarPrecio")
        boolean allowPriceUpdate

) {

    public static CatalogItemResponse from(
            InventoryCatalogItem item
    ) {

        return new CatalogItemResponse(

                item.getId(),

                item.getName(),

                item.getLink(),

                item.isAllowPriceUpdate()
        );
    }
}