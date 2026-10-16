package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UsersPageResponse(

        List<UserAdministrationResponse> content,

        int number,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last,

        @JsonProperty("permisos")
        UsersPagePermissionsResponse permissions

) {
}