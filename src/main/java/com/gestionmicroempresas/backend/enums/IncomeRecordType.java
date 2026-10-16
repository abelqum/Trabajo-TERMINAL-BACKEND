package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum IncomeRecordType {

    INVOICE("FACTURA"),
    INCOME("INGRESO");

    private final String apiValue;


    IncomeRecordType(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }


    public static IncomeRecordType fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El tipo de registro es obligatorio."
            );
        }


        String normalizedValue =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        for (
                IncomeRecordType type :
                values()
        ) {

            if (
                    type.apiValue.equals(
                            normalizedValue
                    )
            ) {

                return type;
            }
        }


        throw new IllegalArgumentException(
                "El tipo de registro debe ser FACTURA o INGRESO."
        );
    }
}