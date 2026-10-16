package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;
import com.gestionmicroempresas.backend.entity.InventoryProduct;

public record KitAvailableProductResponse(

        Long id,


        @JsonProperty("descripcion")
        String description,


        @JsonProperty("modelo")
        String model,


        @JsonProperty("cantidad")
        int quantity,


        @JsonProperty("marca")
        CatalogItemResponse brand,


        @JsonProperty("medida")
        CatalogItemResponse measure,


        @JsonProperty("condicion")
        CatalogItemResponse condition,


        @JsonProperty("proveedor")
        CatalogItemResponse supplier

) {

    public static KitAvailableProductResponse from(
            InventoryProduct product
    ) {

        return new KitAvailableProductResponse(

                product.getId(),

                product.getDescription(),

                product.getModel(),

                product.getQuantity(),

                catalog(
                        product.getBrand()
                ),

                catalog(
                        product.getMeasure()
                ),

                catalog(
                        product.getCondition()
                ),

                catalog(
                        product.getSupplier()
                )
        );
    }


    private static CatalogItemResponse catalog(
            InventoryCatalogItem item
    ) {

        if (item == null) {
            return null;
        }


        return CatalogItemResponse.from(
                item
        );
    }
}