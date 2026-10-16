package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.Role;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter
        implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(
            Role role
    ) {

        if (role == null) {
            return null;
        }

        return role.getApiValue();
    }

    @Override
    public Role convertToEntityAttribute(
            String value
    ) {

        if (value == null) {
            return null;
        }

        return Role.fromApiValue(value);
    }
}