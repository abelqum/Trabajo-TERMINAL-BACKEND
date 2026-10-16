package com.gestionmicroempresas.backend.exception;

import com.gestionmicroempresas.backend.config.RequestIdFilter;

import com.gestionmicroempresas.backend.dto.response.ApiErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );


    // =====================================================
    // 401 - AUTHENTICATION
    // =====================================================

    @ExceptionHandler(
            AuthenticationException.class
    )
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException exception
    ) {

        return response(

                HttpStatus.UNAUTHORIZED,

                "Correo o contraseña incorrectos."
        );
    }


    // =====================================================
    // 403 - ACCESS DENIED
    // =====================================================

    @ExceptionHandler(
            AccessDeniedException.class
    )
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception
    ) {

        return response(

                HttpStatus.FORBIDDEN,

                "No tienes permisos para realizar esta operación."
        );
    }


    // =====================================================
    // 404 - NOT FOUND
    // =====================================================

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {

        return response(

                HttpStatus.NOT_FOUND,

                exception.getMessage()
        );
    }


    // =====================================================
    // 409 - BUSINESS CONFLICT
    // =====================================================

    @ExceptionHandler(
            ConflictException.class
    )
    public ResponseEntity<ApiErrorResponse> handleConflictException(
            ConflictException exception
    ) {

        return response(

                HttpStatus.CONFLICT,

                exception.getMessage()
        );
    }


    // =====================================================
    // 409 - DATABASE INTEGRITY
    // =====================================================

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {

        LOGGER.warn(

                "Database integrity violation: requestId={}",

                currentRequestId()
        );


        return response(

                HttpStatus.CONFLICT,

                "La operación entra en conflicto con información existente."
        );
    }


    // =====================================================
    // 429 - RATE LIMIT
    // =====================================================

    @ExceptionHandler(
            TooManyRequestsException.class
    )
    public ResponseEntity<ApiErrorResponse> handleTooManyRequestsException(
            TooManyRequestsException exception
    ) {

        long retryAfterSeconds =
                exception.getRetryAfterSeconds();


        return ResponseEntity

                .status(
                        HttpStatus.TOO_MANY_REQUESTS
                )

                .header(
                        HttpHeaders.RETRY_AFTER,
                        Long.toString(
                                retryAfterSeconds
                        )
                )

                .body(
                        ApiErrorResponse.of(

                                HttpStatus.TOO_MANY_REQUESTS.value(),

                                exception.getMessage(),

                                currentRequestId()
                        )
                );
    }


    // =====================================================
    // 400 - BEAN VALIDATION
    // =====================================================

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        String message =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(
                                error ->
                                        error.getDefaultMessage()
                        )
                        .orElse(
                                "Los datos enviados no son válidos."
                        );


        return response(

                HttpStatus.BAD_REQUEST,

                message
        );
    }


    // =====================================================
    // 400 - INVALID JSON
    // =====================================================

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ) {

        return response(

                HttpStatus.BAD_REQUEST,

                "El contenido enviado no tiene un formato válido."
        );
    }


    // =====================================================
    // 400 - MISSING PARAMETER
    // =====================================================

    @ExceptionHandler(
            MissingServletRequestParameterException.class
    )
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception
    ) {

        return response(

                HttpStatus.BAD_REQUEST,

                "Falta el parámetro requerido: "
                        +
                        exception.getParameterName()
                        +
                        "."
        );
    }


    // =====================================================
    // 400 - INVALID PARAMETER TYPE
    // =====================================================

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {

        return response(

                HttpStatus.BAD_REQUEST,

                "El parámetro "
                        +
                        exception.getName()
                        +
                        " no tiene un valor válido."
        );
    }


    // =====================================================
    // 400 - INVALID ARGUMENT
    // =====================================================

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {

        return response(

                HttpStatus.BAD_REQUEST,

                exception.getMessage()
        );
    }


    // =====================================================
    // 413 - APPLICATION UPLOAD LIMIT
    // =====================================================

    @ExceptionHandler(
            UploadTooLargeException.class
    )
    public ResponseEntity<ApiErrorResponse> handleUploadTooLargeException(
            UploadTooLargeException exception
    ) {

        return response(

                HttpStatus.PAYLOAD_TOO_LARGE,

                exception.getMessage()
        );
    }


    // =====================================================
    // 413 - SPRING MULTIPART LIMIT
    // =====================================================

    @ExceptionHandler(
            MaxUploadSizeExceededException.class
    )
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception
    ) {

        return response(

                HttpStatus.PAYLOAD_TOO_LARGE,

                "Uno de los archivos supera el tamaño permitido."
        );
    }


    // =====================================================
    // 500 - STORAGE
    // =====================================================

    @ExceptionHandler(
            FileStorageException.class
    )
    public ResponseEntity<ApiErrorResponse> handleFileStorageException(
            FileStorageException exception
    ) {

        String requestId =
                currentRequestId();


        /*
         * Detailed information stays in the server logs.
         * It is not returned to the browser.
         */
        LOGGER.error(

                "File storage operation failed: requestId={}",

                requestId,

                exception
        );


        return ResponseEntity

                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )

                .body(
                        ApiErrorResponse.of(

                                HttpStatus.INTERNAL_SERVER_ERROR.value(),

                                "No se pudo completar la operación con el archivo.",

                                requestId
                        )
                );
    }


    // =====================================================
    // 500 - UNEXPECTED ERROR
    // =====================================================

    @ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception
    ) {

        String requestId =
                currentRequestId();


        /*
         * The stack trace is recorded only on the server.
         *
         * Never return exception.getMessage() here because
         * unexpected exceptions may contain implementation,
         * database or infrastructure details.
         */
        LOGGER.error(

                "Unexpected application error: requestId={}",

                requestId,

                exception
        );


        return ResponseEntity

                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )

                .body(
                        ApiErrorResponse.of(

                                HttpStatus.INTERNAL_SERVER_ERROR.value(),

                                "Ocurrió un error inesperado. Intenta nuevamente.",

                                requestId
                        )
                );
    }


    // =====================================================
    // RESPONSE
    // =====================================================

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String message
    ) {

        return ResponseEntity

                .status(
                        status
                )

                .body(
                        ApiErrorResponse.of(

                                status.value(),

                                message,

                                currentRequestId()
                        )
                );
    }


    // =====================================================
    // CURRENT REQUEST ID
    // =====================================================

    private String currentRequestId() {

        String requestId =
                MDC.get(
                        RequestIdFilter.REQUEST_ID_MDC_KEY
                );


        /*
         * Normally every HTTP request already went through
         * RequestIdFilter.
         *
         * This fallback prevents null values if the advice
         * is ever invoked outside that normal flow.
         */
        if (
                requestId == null
                        ||
                        requestId.isBlank()
        ) {

            return "unavailable";
        }


        return requestId;
    }
}