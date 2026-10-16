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
public class InventoryPhotoStorageService {

    private final TenantStoragePathService pathService;

    private final CurrentTenantIdentityService currentTenantIdentityService;

    private final ImageUploadValidator imageUploadValidator;

    private final String backendUrl;


    public InventoryPhotoStorageService(
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
    // STORE PHOTO
    // =====================================================

    public String storePhoto(
            MultipartFile file
    ) {

        /*
         * Product photo is optional.
         */
        if (
                file == null
                        ||
                        file.isEmpty()
        ) {

            return null;
        }


        ValidatedImage validatedImage =
                imageUploadValidator.validate(

                        file,

                        ImageUploadPolicy.INVENTORY_PHOTO
                );


        String fileName =
                UUID.randomUUID()
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
                    "No se pudo guardar la fotografía del producto.",
                    exception
            );
        }


        return fileName;
    }


    // =====================================================
    // DUPLICATE PHOTO
    // =====================================================

    public String duplicatePhoto(
            String sourceFileName
    ) {

        if (
                sourceFileName == null
                        ||
                        sourceFileName.isBlank()
        ) {

            return null;
        }


        Long tenantId =
                currentTenantIdentityService
                        .getCurrentTenantId();


        Path source =
                pathService.resolveFile(

                        tenantId,

                        StorageArea.INVENTORY,

                        sourceFileName
                );


        if (
                !Files.exists(
                        source
                )
                        ||
                        !Files.isRegularFile(
                                source
                        )
        ) {

            return null;
        }


        String extension =
                getExtension(
                        sourceFileName
                );


        String targetFileName =
                UUID.randomUUID()
                        +
                        extension;


        Path target =
                pathService.resolveFile(

                        tenantId,

                        StorageArea.INVENTORY,

                        targetFileName
                );


        try {

            Files.copy(
                    source,
                    target
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "No se pudo duplicar la fotografía del producto.",
                    exception
            );
        }


        return targetFileName;
    }


    // =====================================================
    // LOAD
    // =====================================================

    public Resource load(
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
                    "La fotografía solicitada no existe."
            );
        }


        try {

            return new UrlResource(
                    path.toUri()
            );

        } catch (Exception exception) {

            throw new FileStorageException(
                    "No se pudo leer la fotografía.",
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

        try {

            String contentType =
                    Files.probeContentType(

                            resolveCurrentTenantPath(
                                    fileName
                            )
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
    // DELETE
    // =====================================================

    public void delete(
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
                    "No se pudo eliminar la fotografía.",
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
                "/api/archivos/inventario/"
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

                StorageArea.INVENTORY,

                fileName
        );
    }


    // =====================================================
    // EXTENSION
    // =====================================================

    private String getExtension(
            String fileName
    ) {

        int index =
                fileName.lastIndexOf(
                        '.'
                );


        if (
                index < 0
                        ||
                        index
                                ==
                                fileName.length() - 1
        ) {

            /*
             * New uploaded files are always WebP.
             *
             * This fallback mainly protects existing
             * development files with an unexpected name.
             */
            return ".webp";
        }


        return fileName.substring(
                index
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