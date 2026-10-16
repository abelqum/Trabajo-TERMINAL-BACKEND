package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum InventoryMovementType {

    INITIAL,

    ENTRY,

    EXIT,

    KIT_ASSEMBLY,

    KIT_DISASSEMBLY;


    public static InventoryMovementType fromAdjustmentApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El tipo de ajuste es obligatorio."
            );
        }


        String normalized =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        return switch (normalized) {

            case "ENTRADA" ->
                    ENTRY;

            case "SALIDA" ->
                    EXIT;

            default ->
                    throw new IllegalArgumentException(
                            "El tipo de ajuste debe ser ENTRADA o SALIDA."
                    );
        };
    }
}