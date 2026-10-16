package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InventoryCatalogsResponse(

        @JsonProperty("unidadesMedida")
        List<CatalogItemResponse> unitsOfMeasure,


        @JsonProperty("marcas")
        List<CatalogItemResponse> brands,


        @JsonProperty("almacenes")
        List<CatalogItemResponse> warehouses,


        @JsonProperty("condiciones")
        List<CatalogItemResponse> conditions,


        @JsonProperty("categorias")
        List<CatalogItemResponse> categories,


        @JsonProperty("proveedores")
        List<CatalogItemResponse> suppliers,


        @JsonProperty("medidas")
        List<CatalogItemResponse> measures

) {
}