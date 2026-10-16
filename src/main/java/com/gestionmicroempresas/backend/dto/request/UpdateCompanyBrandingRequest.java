package com.gestionmicroempresas.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCompanyBrandingRequest(

        @JsonProperty("nombre")
        @NotBlank(
                message =
                        "El nombre de la empresa es obligatorio."
        )
        @Size(
                min = 2,
                max = 200,
                message =
                        "El nombre de la empresa debe tener entre 2 y 200 caracteres."
        )
        String name,


        @JsonProperty("colorPrincipal")
        @NotBlank(
                message =
                        "El color principal es obligatorio."
        )
        @Pattern(
                regexp =
                        "^#[0-9A-Fa-f]{6}$",

                message =
                        "El color principal no es válido."
        )
        String primaryColor,


        @JsonProperty("colorSidebar")
        @NotBlank(
                message =
                        "El color del menú lateral es obligatorio."
        )
        @Pattern(
                regexp =
                        "^#[0-9A-Fa-f]{6}$",

                message =
                        "El color del menú lateral no es válido."
        )
        String sidebarColor

) {
}