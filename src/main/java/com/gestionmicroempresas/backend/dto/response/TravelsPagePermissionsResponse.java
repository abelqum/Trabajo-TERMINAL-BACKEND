package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TravelsPagePermissionsResponse(

        @JsonProperty("crearViaje")
        boolean createTravel

) {
}