package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;
import com.gestionmicroempresas.backend.entity.InventoryProduct;

import java.math.BigDecimal;

public record InventoryProductResponse(

        Long id,


        @JsonProperty("descripcion")
        String description,


        @JsonProperty("modelo")
        String model,


        @JsonProperty("cantidad")
        int quantity,


        @JsonProperty("stockMinimo")
        int minimumStock,


        @JsonProperty("precioUnitario")
        BigDecimal unitPrice,


        @JsonProperty("fila")
        String rowLocation,


        @JsonProperty("esKit")
        boolean kit,


        @JsonProperty("fotoUrl")
        String photoUrl,


        @JsonProperty("codigoQr")
        String qrCode,


        @JsonProperty("categoria")
        CatalogItemResponse category,


        @JsonProperty("marca")
        CatalogItemResponse brand,


        @JsonProperty("unidadMedida")
        CatalogItemResponse unitMeasure,


        @JsonProperty("almacen")
        CatalogItemResponse warehouse,


        @JsonProperty("condicion")
        CatalogItemResponse condition,


        @JsonProperty("medida")
        CatalogItemResponse measure,


        @JsonProperty("proveedor")
        CatalogItemResponse supplier,


        @JsonProperty("permisos")
        InventoryProductPermissionsResponse permissions

) {

    public static InventoryProductResponse from(
            InventoryProduct product,
            String photoUrl,
            InventoryProductPermissionsResponse permissions
    ) {

        return new InventoryProductResponse(

                product.getId(),

                product.getDescription(),

                product.getModel(),

                product.getQuantity(),

                product.getMinimumStock(),

                product.getUnitPrice(),

                product.getRowLocation(),

                product.isKit(),

                photoUrl,

                product.getQrCode(),

                catalog(
                        product.getCategory()
                ),

                catalog(
                        product.getBrand()
                ),

                catalog(
                        product.getUnitMeasure()
                ),

                catalog(
                        product.getWarehouse()
                ),

                catalog(
                        product.getCondition()
                ),

                catalog(
                        product.getMeasure()
                ),

                catalog(
                        product.getSupplier()
                ),

                permissions
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