package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InventoryProductPermissionsResponse(

        @JsonProperty("editar")
        boolean edit,

        @JsonProperty("eliminar")
        boolean delete,

        @JsonProperty("ajustarStock")
        boolean adjustStock,

        @JsonProperty("duplicar")
        boolean duplicate,

        @JsonProperty("armarKit")
        boolean assembleKit,

        @JsonProperty("desarmarKit")
        boolean disassembleKit

) {
}