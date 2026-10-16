package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum ExpenseCategory {

    OPERATION("OPERACION"),

    SERVICES("SERVICIOS"),

    RENT("RENTA"),

    PAYROLL("NOMINA"),

    PURCHASES("COMPRAS"),

    MAINTENANCE("MANTENIMIENTO"),

    TAXES("IMPUESTOS"),

    ADVERTISING("PUBLICIDAD"),

    TRANSPORTATION("TRANSPORTE"),

    OTHER("OTROS");

    private final String apiValue;


    ExpenseCategory(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }


    public static ExpenseCategory fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La categoría es obligatoria."
            );
        }


        String normalizedValue =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        for (
                ExpenseCategory category :
                values()
        ) {

            if (
                    category.apiValue.equals(
                            normalizedValue
                    )
            ) {

                return category;
            }
        }


        throw new IllegalArgumentException(
                "La categoría del egreso no es válida."
        );
    }
}