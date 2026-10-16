package com.gestionmicroempresas.backend.storage;

public enum ImageUploadPolicy {

    COMPANY_LOGO(
            "El logotipo",
            256L * 1024L
    ),

    INVENTORY_PHOTO(
            "La fotografía del producto",
            768L * 1024L
    ),

    TASK_EVIDENCE(
            "La evidencia",
            1024L * 1024L
    ),

    TRAVEL_RECEIPT(
            "El comprobante",
            1024L * 1024L
    );


    private final String displayName;

    private final long maxBytes;


    ImageUploadPolicy(
            String displayName,
            long maxBytes
    ) {

        this.displayName =
                displayName;

        this.maxBytes =
                maxBytes;
    }


    public String getDisplayName() {
        return displayName;
    }


    public long getMaxBytes() {
        return maxBytes;
    }


    public long getMaxKilobytes() {

        return maxBytes / 1024L;
    }
}