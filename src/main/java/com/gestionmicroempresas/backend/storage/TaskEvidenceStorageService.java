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
public class TaskEvidenceStorageService {

    private final TenantStoragePathService pathService;

    private final CurrentTenantIdentityService currentTenantIdentityService;

    private final ImageUploadValidator imageUploadValidator;

    private final String backendUrl;


    public TaskEvidenceStorageService(
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
    // STORE
    // =====================================================

    public StoredEvidence store(
            MultipartFile file
    ) {

        ValidatedImage validatedImage =
                imageUploadValidator.validate(

                        file,

                        ImageUploadPolicy.TASK_EVIDENCE
                );


        String extension =
                validatedImage.extension();


        String fileName =
                UUID.randomUUID()
                        +
                        extension;


        Path destination =
                resolveCurrentTenantPath(
                        fileName
                );


        try {

            Files.copy(

                    file.getInputStream(),

                    destination,

                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "No se pudo guardar la evidencia.",
                    exception
            );
        }


        String originalName =
                normalizeOriginalName(

                        file.getOriginalFilename(),

                        extension
                );


        return new StoredEvidence(

                fileName,

                originalName,

                validatedImage.contentType(),

                validatedImage.size()
        );
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
                    "La evidencia solicitada no existe."
            );
        }


        try {

            return new UrlResource(
                    path.toUri()
            );

        } catch (Exception exception) {

            throw new FileStorageException(
                    "No se pudo leer la evidencia.",
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
                    "No se pudo eliminar la evidencia.",
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
                "/api/archivos/tareas/"
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

                StorageArea.TASKS,

                fileName
        );
    }


    // =====================================================
    // ORIGINAL FILE NAME
    // =====================================================

    private String normalizeOriginalName(
            String originalName,
            String extension
    ) {

        String normalized =
                originalName == null
                        ||
                        originalName.isBlank()

                        ?
                        "Evidencia"
                        +
                        extension

                        :
                        Path
                                .of(
                                        originalName
                                )
                                .getFileName()
                                .toString()
                                .trim();


        if (
                normalized.length()
                        >
                        255
        ) {

            normalized =
                    normalized.substring(
                            0,
                            255
                    );
        }


        return normalized;
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


    // =====================================================
    // STORED EVIDENCE
    // =====================================================

    public record StoredEvidence(

            String fileName,

            String originalName,

            String mimeType,

            long sizeBytes

    ) {
    }
}