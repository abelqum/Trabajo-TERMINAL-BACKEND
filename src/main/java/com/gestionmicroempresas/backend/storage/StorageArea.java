package com.gestionmicroempresas.backend.storage;

public enum StorageArea {

    COMPANY("company"),

    INVENTORY("inventory"),

    TASKS("tasks"),

    VIATICS("viatics");


    private final String directoryName;


    StorageArea(
            String directoryName
    ) {

        this.directoryName =
                directoryName;
    }


    public String getDirectoryName() {
        return directoryName;
    }
}