package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum TravelStatus {

    ACTIVE("ACTIVO"),
    FINISHED("FINALIZADO");

    private final String apiValue;


    TravelStatus(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }


    public static TravelStatus fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El estatus del viaje es obligatorio."
            );
        }


        String normalizedValue =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        for (TravelStatus status : values()) {

            if (
                    status.apiValue.equals(
                            normalizedValue
                    )
            ) {

                return status;
            }
        }


        throw new IllegalArgumentException(
                "El estatus del viaje debe ser ACTIVO o FINALIZADO."
        );
    }
}