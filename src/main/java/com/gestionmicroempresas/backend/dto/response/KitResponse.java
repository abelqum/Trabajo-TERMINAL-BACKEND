package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

import java.util.List;

public record KitResponse(

        Long id,


        @JsonProperty("descripcion")
        String description,


        @JsonProperty("cantidad")
        int quantity,


        @JsonProperty("precioUnitario")
        BigDecimal unitPrice,


        @JsonProperty("fila")
        String rowLocation,


        @JsonProperty("fotoUrl")
        String photoUrl,


        @JsonProperty("categoria")
        CatalogItemResponse category,


        @JsonProperty("almacen")
        CatalogItemResponse warehouse,


        @JsonProperty("componentes")
        List<KitComponentResponse> components

) {
}