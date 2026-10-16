package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum IncomeStatus {

    PENDING("PENDIENTE"),
    PAID("PAGADO"),
    CANCELED("CANCELADO");

    private final String apiValue;


    IncomeStatus(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }


    public static IncomeStatus fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El estado no puede estar vacío."
            );
        }


        String normalizedValue =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        for (
                IncomeStatus status :
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
                "El estado debe ser PENDIENTE, PAGADO o CANCELADO."
        );
    }
}