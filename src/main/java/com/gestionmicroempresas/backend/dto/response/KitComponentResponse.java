package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KitComponentResponse(

        @JsonProperty("productoId")
        Long productId,


        @JsonProperty("producto")
        KitAvailableProductResponse product,


        @JsonProperty("cantidadNecesaria")
        int requiredQuantity

) {
}