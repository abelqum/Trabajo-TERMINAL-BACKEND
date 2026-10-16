package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.Task;

import java.time.LocalDate;

public record DashboardTaskResponse(

        Long id,


        @JsonProperty("titulo")
        String title,


        @JsonProperty("descripcion")
        String description,


        @JsonProperty("fechaLimite")
        LocalDate dueDate,


        @JsonProperty("prioridad")
        String priority,


        @JsonProperty("estado")
        String status

) {

    public static DashboardTaskResponse from(
            Task task
    ) {

        LocalDate dueDate =
                task.getDueAt() == null
                        ?
                        null
                        :
                        task
                                .getDueAt()
                                .toLocalDate();


        return new DashboardTaskResponse(

                task.getId(),

                task.getTitle(),

                task.getDescription(),

                dueDate,

                task
                        .getPriority()
                        .getApiValue(),

                task
                        .getStatus()
                        .getApiValue()
        );
    }
}