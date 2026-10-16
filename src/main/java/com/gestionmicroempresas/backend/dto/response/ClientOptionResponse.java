package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.Client;

public record ClientOptionResponse(

        Long id,

        @JsonProperty("razonSocial")
        String businessName,

        String rfc

) {

    public static ClientOptionResponse from(
            Client client
    ) {

        return new ClientOptionResponse(

                client.getId(),

                client.getBusinessName(),

                client.getRfc()
        );
    }
}