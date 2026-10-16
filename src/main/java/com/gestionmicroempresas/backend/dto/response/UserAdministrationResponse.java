package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.User;

import java.time.OffsetDateTime;

public record UserAdministrationResponse(

        Long id,

        @JsonProperty("nombre")
        String name,

        String email,

        @JsonProperty("rol")
        String role,

        @JsonProperty("creadoEn")
        OffsetDateTime createdAt,

        @JsonProperty("actualizadoEn")
        OffsetDateTime updatedAt,

        @JsonProperty("permisos")
        UserPermissionsResponse permissions

) {

    public static UserAdministrationResponse from(
            User user,
            UserPermissionsResponse permissions
    ) {

        return new UserAdministrationResponse(

                user.getId(),

                user.getName(),

                user.getEmail(),

                user
                        .getRole()
                        .getApiValue(),

                user.getCreatedAt(),

                user.getUpdatedAt(),

                permissions
        );
    }
}