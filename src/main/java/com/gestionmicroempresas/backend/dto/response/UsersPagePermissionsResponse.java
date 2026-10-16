package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsersPagePermissionsResponse(

        @JsonProperty("crearUsuario")
        boolean createUser

) {
}