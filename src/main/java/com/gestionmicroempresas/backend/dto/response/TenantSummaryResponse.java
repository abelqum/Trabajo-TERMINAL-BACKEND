package com.gestionmicroempresas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.gestionmicroempresas.backend.entity.Tenant;

public record TenantSummaryResponse(

        Long id,


        @JsonProperty("nombre")
        String name,


        @JsonProperty("logoUrl")
        String logoUrl,


        @JsonProperty("colorPrincipal")
        String primaryColor,


        @JsonProperty("colorSidebar")
        String sidebarColor

) {

    public static TenantSummaryResponse from(
            Tenant tenant
    ) {

        String logoUrl =
                null;


        if (
                tenant.getLogoFileName()
                        != null
                        &&
                        !tenant
                                .getLogoFileName()
                                .isBlank()
        ) {

            long version =
                    tenant.getUpdatedAt() == null
                            ?
                            0
                            :
                            tenant
                                    .getUpdatedAt()
                                    .toInstant()
                                    .toEpochMilli();


            logoUrl =
                    "/archivos/empresa/logo?v="
                            +
                            version;
        }


        return new TenantSummaryResponse(
                tenant.getId(),
                tenant.getName(),
                logoUrl,
                tenant.getPrimaryColor(),
                tenant.getSidebarColor()
        );
    }
}