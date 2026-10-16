package com.gestionmicroempresas.backend.storage;

import com.gestionmicroempresas.backend.exception.FileStorageException;
import com.gestionmicroempresas.backend.exception.UploadTooLargeException;

import org.springframework.stereotype.Component;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.Locale;

@Component
public class ImageUploadValidator {

    private static final String WEBP_CONTENT_TYPE =
            "image/webp";


    private static final String WEBP_EXTENSION =
            ".webp";


    private static final int WEBP_HEADER_LENGTH =
            12;


    // =====================================================
    // VALIDATE
    // =====================================================

    public ValidatedImage validate(
            MultipartFile file,
            ImageUploadPolicy policy
    ) {

        validatePresence(
                file,
                policy
        );


        validateSize(
                file,
                policy
        );


        validateDeclaredContentType(
                file,
                policy
        );


        validateRealContent(
                file,
                policy
        );


        validateOriginalFileName(
                file
        );


        return new ValidatedImage(

                WEBP_CONTENT_TYPE,

                WEBP_EXTENSION,

                file.getSize()
        );
    }


    // =====================================================
    // PRESENCE
    // =====================================================

    private void validatePresence(
            MultipartFile file,
            ImageUploadPolicy policy
    ) {

        if (
                file == null
                        ||
                        file.isEmpty()
        ) {

            throw new IllegalArgumentException(

                    policy.getDisplayName()
                            +
                            " está vacío."
            );
        }
    }


    // =====================================================
    // SIZE
    // =====================================================

    private void validateSize(
            MultipartFile file,
            ImageUploadPolicy policy
    ) {

        if (
                file.getSize()
                        >
                        policy.getMaxBytes()
        ) {

            throw new UploadTooLargeException(

                    policy.getDisplayName()
                            +
                            " supera el tamaño máximo permitido de "
                            +
                            policy.getMaxKilobytes()
                            +
                            " KB."
            );
        }
    }


    // =====================================================
    // DECLARED CONTENT TYPE
    // =====================================================

    private void validateDeclaredContentType(
            MultipartFile file,
            ImageUploadPolicy policy
    ) {

        String contentType =
                file.getContentType();


        if (
                contentType == null
                        ||
                        !WEBP_CONTENT_TYPE.equals(
                                contentType
                                        .trim()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                        )
        ) {

            throw new IllegalArgumentException(

                    policy.getDisplayName()
                            +
                            " debe estar en formato WebP."
            );
        }
    }


    // =====================================================
    // REAL FILE SIGNATURE
    // =====================================================

    private void validateRealContent(
            MultipartFile file,
            ImageUploadPolicy policy
    ) {

        byte[] header;


        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {

            header =
                    inputStream.readNBytes(
                            WEBP_HEADER_LENGTH
                    );

        } catch (IOException exception) {

            throw new FileStorageException(

                    "No se pudo validar el archivo recibido.",

                    exception
            );
        }


        if (
                !isWebp(
                        header
                )
        ) {

            throw new IllegalArgumentException(

                    policy.getDisplayName()
                            +
                            " no contiene una imagen WebP válida."
            );
        }
    }


    // =====================================================
    // WEBP SIGNATURE
    // =====================================================

    private boolean isWebp(
            byte[] header
    ) {

        if (
                header == null
                        ||
                        header.length
                                <
                                WEBP_HEADER_LENGTH
        ) {

            return false;
        }


        String riff =
                new String(

                        header,

                        0,

                        4,

                        StandardCharsets.US_ASCII
                );


        String webp =
                new String(

                        header,

                        8,

                        4,

                        StandardCharsets.US_ASCII
                );


        return "RIFF".equals(
                riff
        )
                &&
                "WEBP".equals(
                        webp
                );
    }


    // =====================================================
    // ORIGINAL NAME
    // =====================================================

    private void validateOriginalFileName(
            MultipartFile file
    ) {

        String originalFileName =
                file.getOriginalFilename();


        if (
                originalFileName == null
                        ||
                        originalFileName.isBlank()
        ) {

            return;
        }


        if (
                originalFileName.length()
                        >
                        255
        ) {

            throw new IllegalArgumentException(
                    "El nombre del archivo es demasiado largo."
            );
        }


        if (
                originalFileName.indexOf('\0')
                        >=
                        0
        ) {

            throw new IllegalArgumentException(
                    "El nombre del archivo no es válido."
            );
        }
    }
}