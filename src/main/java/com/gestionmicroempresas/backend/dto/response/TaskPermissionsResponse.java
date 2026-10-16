package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TaskPermissionsResponse(

        @JsonProperty("editar")
        boolean edit,

        @JsonProperty("eliminar")
        boolean delete,

        @JsonProperty("cambiarPrioridad")
        boolean changePriority,

        @JsonProperty("subirEvidencias")
        boolean uploadEvidence,

        @JsonProperty("retroalimentar")
        boolean giveFeedback,

        @JsonProperty("estadosPermitidos")
        List<String> allowedStatuses

) {
}