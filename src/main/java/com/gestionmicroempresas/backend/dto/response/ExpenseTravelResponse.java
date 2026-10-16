package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.Travel;

public record ExpenseTravelResponse(

        Long id,

        @JsonProperty("nombre")
        String name,

        @JsonProperty("destino")
        String destination

) {

    public static ExpenseTravelResponse from(
            Travel travel
    ) {

        return new ExpenseTravelResponse(

                travel.getId(),

                travel.getName(),

                travel.getDestination()
        );
    }
}