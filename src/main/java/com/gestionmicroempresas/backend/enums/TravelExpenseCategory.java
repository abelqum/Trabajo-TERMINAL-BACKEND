package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum TravelExpenseCategory {

    FOOD("ALIMENTOS"),
    GASOLINE("GASOLINA"),
    TOLLS("CASETAS"),
    LODGING("HOSPEDAJE"),
    MATERIAL("MATERIAL"),
    OTHER("OTROS");

    private final String apiValue;


    TravelExpenseCategory(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }


    public static TravelExpenseCategory fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La categoría del gasto es obligatoria."
            );
        }


        String normalizedValue =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        for (
                TravelExpenseCategory category :
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
                "La categoría del gasto no es válida."
        );
    }
}