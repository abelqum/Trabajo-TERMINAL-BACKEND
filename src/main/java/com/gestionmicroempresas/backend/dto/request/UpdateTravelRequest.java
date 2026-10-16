package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import java.util.List;

public record UpdateTravelRequest(

        @JsonProperty("nombre")
        @NotBlank(
                message =
                        "El nombre del viaje es obligatorio."
        )
        @Size(
                max = 150,
                message =
                        "El nombre no puede superar los 150 caracteres."
        )
        String name,


        @JsonProperty("destino")
        @NotBlank(
                message =
                        "El destino es obligatorio."
        )
        @Size(
                max = 150,
                message =
                        "El destino no puede superar los 150 caracteres."
        )
        String destination,


        @JsonProperty("fechaInicio")
        @NotNull(
                message =
                        "La fecha de inicio es obligatoria."
        )
        LocalDate startDate,


        @JsonProperty("fechaFin")
        @NotNull(
                message =
                        "La fecha de fin es obligatoria."
        )
        LocalDate endDate,


        @JsonProperty("usuariosIds")
        @NotEmpty(
                message =
                        "Asigna al menos a una persona."
        )
        List<@NotNull Long> userIds,


        @JsonProperty("presupuesto")
        @NotNull(
                message =
                        "El presupuesto es obligatorio."
        )
        @Valid
        TravelBudgetRequest budget

) {
}