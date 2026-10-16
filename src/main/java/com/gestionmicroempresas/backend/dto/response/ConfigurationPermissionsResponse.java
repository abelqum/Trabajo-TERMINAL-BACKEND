package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConfigurationPermissionsResponse(

        @JsonProperty("administrarCatalogos")
        boolean manageCatalogs,


        @JsonProperty("administrarKits")
        boolean manageKits,


        @JsonProperty("descargarRespaldo")
        boolean downloadBackup

) {
}