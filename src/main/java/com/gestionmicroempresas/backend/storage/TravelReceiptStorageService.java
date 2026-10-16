package com.gestionmicroempresas.backend.storage;

import com.gestionmicroempresas.backend.exception.FileStorageException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.service.CurrentTenantIdentityService;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.UUID;

@Service
public class TravelReceiptStorageService {

    private final TenantStoragePathService pathService;

    private final CurrentTenantIdentityService currentTenantIdentityService;

    private final ImageUploadValidator imageUploadValidator;

    private final String backendUrl;


    public TravelReceiptStorageService(
            TenantStoragePathService pathService,
            CurrentTenantIdentityService currentTenantIdentityService,
            ImageUploadValidator imageUploadValidator,

            @Value("${app.backend-url}")
            String backendUrl
    ) {

        this.pathService =
                pathService;

        this.currentTenantIdentityService =
                currentTenantIdentityService;

        this.imageUploadValidator =
                imageUploadValidator;

        this.backendUrl =
                removeTrailingSlash(
                        backendUrl
                );
    }


    // =====================================================
    // STORE RECEIPT
    // =====================================================

    public String storeReceipt(
            MultipartFile file
    ) {

        ValidatedImage validatedImage =
                imageUploadValidator.validate(

                        file,

                        ImageUploadPolicy.TRAVEL_RECEIPT
                );


        String fileName =
                UUID
                        .randomUUID()
                        .toString()
                        +
                        validatedImage.extension();


        Path target =
                resolveCurrentTenantPath(
                        fileName
                );


        try {

            Files.copy(

                    file.getInputStream(),

                    target,

                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "No se pudo guardar el comprobante.",
                    exception
            );
        }


        return fileName;
    }


    // =====================================================
    // LOAD RECEIPT
    // =====================================================

    public Resource loadReceipt(
            String fileName
    ) {

        Path path =
                resolveCurrentTenantPath(
                        fileName
                );


        if (
                !Files.exists(
                        path
                )
                        ||
                        !Files.isRegularFile(
                                path
                        )
        ) {

            throw new ResourceNotFoundException(
                    "El comprobante solicitado no existe."
            );
        }


        try {

            return new UrlResource(
                    path.toUri()
            );

        } catch (Exception exception) {

            throw new FileStorageException(
                    "No se pudo leer el comprobante.",
                    exception
            );
        }
    }


    // =====================================================
    // CONTENT TYPE
    // =====================================================

    public String getContentType(
            String fileName
    ) {

        Path path =
                resolveCurrentTenantPath(
                        fileName
                );


        try {

            String contentType =
                    Files.probeContentType(
                            path
                    );


            return contentType != null
                    ?
                    contentType
                    :
                    "application/octet-stream";

        } catch (IOException exception) {

            return "application/octet-stream";
        }
    }


    // =====================================================
    // DELETE RECEIPT
    // =====================================================

    public void deleteReceipt(
            String fileName
    ) {

        if (
                fileName == null
                        ||
                        fileName.isBlank()
        ) {

            return;
        }


        try {

            Files.deleteIfExists(

                    resolveCurrentTenantPath(
                            fileName
                    )
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "No se pudo eliminar el comprobante.",
                    exception
            );
        }
    }


    // =====================================================
    // PUBLIC URL
    // =====================================================

    public String getPublicUrl(
            String fileName
    ) {

        if (
                fileName == null
                        ||
                        fileName.isBlank()
        ) {

            return null;
        }


        return backendUrl
                +
                "/api/archivos/viaticos/"
                +
                fileName;
    }


    // =====================================================
    // CURRENT TENANT PATH
    // =====================================================

    private Path resolveCurrentTenantPath(
            String fileName
    ) {

        return pathService.resolveFile(

                currentTenantIdentityService
                        .getCurrentTenantId(),

                StorageArea.VIATICS,

                fileName
        );
    }


    // =====================================================
    // URL
    // =====================================================

    private String removeTrailingSlash(
            String value
    ) {

        String result =
                value.trim();


        while (
                result.endsWith("/")
        ) {

            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }


        return result;
    }
}