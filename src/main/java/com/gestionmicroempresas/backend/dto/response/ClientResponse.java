package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.Client;

import java.time.OffsetDateTime;

import java.util.List;

public record ClientResponse(

        Long id,

        @JsonProperty("razonSocial")
        String businessName,

        String rfc,

        @JsonProperty("telefonos")
        List<String> phones,

        @JsonProperty("correos")
        List<String> emails,

        @JsonProperty("creadoEn")
        OffsetDateTime createdAt,

        @JsonProperty("actualizadoEn")
        OffsetDateTime updatedAt

) {

    public static ClientResponse from(
            Client client
    ) {

        return new ClientResponse(

                client.getId(),

                client.getBusinessName(),

                client.getRfc(),

                client
                        .getPhones()
                        .stream()
                        .sorted()
                        .toList(),

                client
                        .getEmails()
                        .stream()
                        .sorted()
                        .toList(),

                client.getCreatedAt(),

                client.getUpdatedAt()
        );
    }
}