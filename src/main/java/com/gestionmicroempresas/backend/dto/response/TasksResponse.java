package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TasksResponse(

        @JsonProperty("tareas")
        List<TaskResponse> tasks,


        @JsonProperty("permisos")
        TasksPagePermissionsResponse permissions

) {
}