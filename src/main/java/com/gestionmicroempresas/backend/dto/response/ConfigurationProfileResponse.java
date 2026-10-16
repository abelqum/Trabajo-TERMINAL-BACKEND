package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.User;

public record ConfigurationProfileResponse(

        Long id,


        @JsonProperty("nombre")
        String name,


        String email,


        @JsonProperty("rol")
        String role

) {

    public static ConfigurationProfileResponse from(
            User user
    ) {

        return new ConfigurationProfileResponse(

                user.getId(),

                user.getName(),

                user.getEmail(),

                user
                        .getRole()
                        .getApiValue()
        );
    }
}