package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum TravelExpenseStatus {

    PENDING("PENDIENTE"),
    APPROVED("APROBADO"),
    REJECTED("RECHAZADO");

    private final String apiValue;


    TravelExpenseStatus(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }


    public static TravelExpenseStatus fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El estatus del gasto es obligatorio."
            );
        }


        String normalizedValue =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        for (
                TravelExpenseStatus status :
                values()
        ) {

            if (
                    status.apiValue.equals(
                            normalizedValue
                    )
            ) {

                return status;
            }
        }


        throw new IllegalArgumentException(
                "El estatus debe ser PENDIENTE, APROBADO o RECHAZADO."
        );
    }
}