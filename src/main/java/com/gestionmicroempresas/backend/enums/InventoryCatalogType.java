package com.gestionmicroempresas.backend.enums;

import java.util.Locale;

public enum InventoryCatalogType {

    CATEGORY(
            "categorias",
            false,
            false
    ),

    SUPPLIER(
            "proveedores",
            true,
            false
    ),

    BRAND(
            "marcas",
            false,
            false
    ),

    UNIT_OF_MEASURE(
            "unidades-medida",
            false,
            false
    ),

    WAREHOUSE(
            "almacenes",
            false,
            false
    ),

    CONDITION(
            "condiciones",
            false,
            true
    ),

    MEASURE(
            "medidas",
            false,
            false
    );

    private final String apiValue;

    private final boolean supportsLink;

    private final boolean supportsPriceUpdate;


    InventoryCatalogType(
            String apiValue,
            boolean supportsLink,
            boolean supportsPriceUpdate
    ) {

        this.apiValue =
                apiValue;

        this.supportsLink =
                supportsLink;

        this.supportsPriceUpdate =
                supportsPriceUpdate;
    }


    public String getApiValue() {
        return apiValue;
    }


    public boolean supportsLink() {
        return supportsLink;
    }


    public boolean supportsPriceUpdate() {
        return supportsPriceUpdate;
    }


    public static InventoryCatalogType fromApiValue(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El tipo de catálogo es obligatorio."
            );
        }


        String normalized =
                value
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        for (
                InventoryCatalogType type :
                values()
        ) {

            if (
                    type.apiValue.equals(
                            normalized
                    )
            ) {

                return type;
            }
        }


        throw new IllegalArgumentException(
                "El catálogo solicitado no es válido."
        );
    }
}