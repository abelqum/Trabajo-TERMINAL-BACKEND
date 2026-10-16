package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum TaskPriority {

    URGENT_IMPORTANT("URGENTE_IMPORTANTE"),

    IMPORTANT_NOT_URGENT("IMPORTANTE_NO_URGENTE"),

    URGENT_NOT_IMPORTANT("URGENTE_NO_IMPORTANTE"),

    NOT_URGENT_NOT_IMPORTANT("NO_URGENTE_NO_IMPORTANTE");

    private final String apiValue;

    TaskPriority(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static TaskPriority fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La prioridad es obligatoria."
            );
        }

        String normalized =
                value
                        .trim()
                        .toUpperCase(Locale.ROOT);

        for (TaskPriority priority : values()) {

            if (
                    priority.apiValue.equals(
                            normalized
                    )
            ) {

                return priority;
            }
        }

        throw new IllegalArgumentException(
                "La prioridad de la tarea no es válida."
        );
    }
}