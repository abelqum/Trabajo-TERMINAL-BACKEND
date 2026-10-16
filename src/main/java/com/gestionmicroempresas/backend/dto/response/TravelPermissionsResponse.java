package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TravelPermissionsResponse(

        @JsonProperty("editar")
        boolean edit,

        @JsonProperty("eliminar")
        boolean delete,

        @JsonProperty("registrarGastos")
        boolean registerExpenses,

        @JsonProperty("revisarGastos")
        boolean reviewExpenses,

        @JsonProperty("finalizar")
        boolean finish

) {
}