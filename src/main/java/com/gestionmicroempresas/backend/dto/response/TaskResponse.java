package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

import java.util.List;

public record TaskResponse(

        Long id,


        @JsonProperty("titulo")
        String title,


        @JsonProperty("descripcion")
        String description,


        @JsonProperty("fechaLimite")
        OffsetDateTime dueAt,


        @JsonProperty("prioridad")
        String priority,


        @JsonProperty("estado")
        String status,


        @JsonProperty("creador")
        UserOptionResponse creator,


        @JsonProperty("asignados")
        List<UserOptionResponse> assignees,


        @JsonProperty("retroalimentacion")
        String feedback,


        @JsonProperty("fechaRevision")
        OffsetDateTime reviewedAt,


        @JsonProperty("fechaCompletada")
        OffsetDateTime completedAt,


        @JsonProperty("creadoEn")
        OffsetDateTime createdAt,


        @JsonProperty("actualizadoEn")
        OffsetDateTime updatedAt,


        @JsonProperty("permisos")
        TaskPermissionsResponse permissions

) {
}