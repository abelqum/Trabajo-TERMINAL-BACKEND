package com.gestionmicroempresas.backend.enums;

public enum TenantStatus {

    ACTIVE("ACTIVA"),

    SUSPENDED("SUSPENDIDA");

    private final String apiValue;


    TenantStatus(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }
}