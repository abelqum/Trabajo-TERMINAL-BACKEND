package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConfigurationResponse(

        @JsonProperty("perfil")
        ConfigurationProfileResponse profile,


        @JsonProperty("permisos")
        ConfigurationPermissionsResponse permissions

) {
}