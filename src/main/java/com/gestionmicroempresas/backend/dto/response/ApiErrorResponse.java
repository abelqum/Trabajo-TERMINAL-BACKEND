package com.gestionmicroempresas.backend.dto.response;

import java.time.Instant;

public record ApiErrorResponse(

        int status,

        String message,

        String requestId,

        Instant timestamp

) {

    // =====================================================
    // WITHOUT REQUEST ID
    // =====================================================

    public static ApiErrorResponse of(
            int status,
            String message
    ) {

        return new ApiErrorResponse(

                status,

                message,

                null,

                Instant.now()
        );
    }


    // =====================================================
    // WITH REQUEST ID
    // =====================================================

    public static ApiErrorResponse of(
            int status,
            String message,
            String requestId
    ) {

        return new ApiErrorResponse(

                status,

                message,

                requestId,

                Instant.now()
        );
    }
}