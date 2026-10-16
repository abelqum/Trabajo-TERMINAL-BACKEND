package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum TaskScope {

    ALL("TODAS"),

    MY_TASKS("MIS_TAREAS"),

    ASSIGNED_BY_ME("ASIGNADAS_POR_MI"),

    USER("USUARIO");

    private final String apiValue;

    TaskScope(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static TaskScope fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return ALL;
        }

        String normalized =
                value
                        .trim()
                        .toUpperCase(Locale.ROOT);

        for (TaskScope scope : values()) {

            if (
                    scope.apiValue.equals(
                            normalized
                    )
            ) {

                return scope;
            }
        }

        throw new IllegalArgumentException(
                "El alcance de tareas no es válido."
        );
    }
}