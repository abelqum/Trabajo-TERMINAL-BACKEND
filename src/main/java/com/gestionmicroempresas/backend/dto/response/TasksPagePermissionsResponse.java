package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TasksPagePermissionsResponse(

        @JsonProperty("crearTarea")
        boolean createTask

) {
}