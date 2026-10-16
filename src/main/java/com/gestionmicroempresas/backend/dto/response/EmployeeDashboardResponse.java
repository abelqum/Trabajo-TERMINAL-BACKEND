package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EmployeeDashboardResponse(

        @JsonProperty("tipoVista")
        String viewType,


        @JsonProperty("usuario")
        UserOptionResponse user,


        @JsonProperty("tareasPendientes")
        List<DashboardTaskResponse> pendingTasks

) {
}