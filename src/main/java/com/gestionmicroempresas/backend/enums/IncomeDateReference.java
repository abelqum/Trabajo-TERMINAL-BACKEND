package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum IncomeDateReference {

    RECORD("REGISTRO"),
    PAYMENT("PAGO");

    private final String apiValue;


    IncomeDateReference(
            String apiValue
    ) {

        this.apiValue =
                apiValue;
    }


    public String getApiValue() {
        return apiValue;
    }


    public static IncomeDateReference fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return RECORD;
        }


        String normalizedValue =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        for (
                IncomeDateReference reference :
                values()
        ) {

            if (
                    reference.apiValue.equals(
                            normalizedValue
                    )
            ) {

                return reference;
            }
        }


        throw new IllegalArgumentException(
                "La referencia de fecha debe ser REGISTRO o PAGO."
        );
    }
}