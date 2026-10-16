package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum Role {

    ADMIN("admin"),
    EDITOR("editor"),
    EMPLOYEE("empleado");

    private final String apiValue;

    Role(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static Role fromApiValue(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "El rol no puede estar vacío."
            );
        }

        String normalizedValue =
                value.trim()
                        .toLowerCase(Locale.ROOT);

        for (Role role : values()) {

            if (role.apiValue.equals(normalizedValue)) {
                return role;
            }
        }

        throw new IllegalArgumentException(
                "El rol debe ser admin, editor o empleado."
        );
    }
}