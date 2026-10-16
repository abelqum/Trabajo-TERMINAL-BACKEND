package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserPermissionsResponse(

        @JsonProperty("editar")
        boolean edit,

        @JsonProperty("eliminar")
        boolean delete,

        @JsonProperty("cambiarRol")
        boolean changeRole,

        @JsonProperty("restablecerPassword")
        boolean resetPassword

) {
}