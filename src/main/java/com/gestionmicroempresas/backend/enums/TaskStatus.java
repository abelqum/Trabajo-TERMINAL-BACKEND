package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum TaskStatus {

    PENDING("PENDIENTE"),

    IN_PROGRESS("EN_PROGRESO"),

    REVIEW("REVISION"),

    COMPLETED("COMPLETADA");

    private final String apiValue;

    TaskStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static TaskStatus fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El estado es obligatorio."
            );
        }

        String normalized =
                value
                        .trim()
                        .toUpperCase(Locale.ROOT);

        for (TaskStatus status : values()) {

            if (
                    status.apiValue.equals(
                            normalized
                    )
            ) {

                return status;
            }
        }

        throw new IllegalArgumentException(
                "El estado de la tarea no es válido."
        );
    }
}