package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.User;

public record UserResponse(

        Long id,


        @JsonProperty("nombre")
        String name,


        String email,


        @JsonProperty("rol")
        String role,


        @JsonProperty("empresa")
        TenantSummaryResponse tenant

) {

    public static UserResponse from(
            User user
    ) {

        return new UserResponse(

                user.getId(),

                user.getName(),

                user.getEmail(),

                user
                        .getRole()
                        .getApiValue(),

                TenantSummaryResponse.from(
                        user.getTenant()
                )
        );
    }
}