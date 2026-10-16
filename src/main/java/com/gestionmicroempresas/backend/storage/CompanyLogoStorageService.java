package com.gestionmicroempresas.backend.storage;

import com.gestionmicroempresas.backend.exception.FileStorageException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.service.CurrentTenantIdentityService;

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
public class CompanyLogoStorageService {

    /*
     * The frontend targets approximately 150 KB.
     *
     * The backend accepts up to 250 KB to leave some
     * margin for WebP encoding differences.
     */
    private static final long MAX_LOGO_BYTES =
            250L * 1024L;


    private final TenantStoragePathService pathService;

    private final CurrentTenantIdentityService currentTenantIdentityService;

    private final ImageUploadValidator imageUploadValidator;
    public CompanyLogoStorageService(
            TenantStoragePathService pathService,
            CurrentTenantIdentityService currentTenantIdentityService,
            ImageUploadValidator imageUploadValidator
    ) {

        this.pathService =
                pathService;

        this.currentTenantIdentityService =
                currentTenantIdentityService;
        this.imageUploadValidator =
                imageUploadValidator;
    }


    public String store(
            MultipartFile file
    ) {

        if (
                file == null
                        ||
                        file.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Selecciona un logo."
            );
        }


        if (
                file.getSize()
                        >
                        MAX_LOGO_BYTES
        ) {

            throw new IllegalArgumentException(
                    "El logo optimizado no puede superar los 250 KB."
            );
        }


        if (
                !"image/webp"
                        .equalsIgnoreCase(
                                file.getContentType()
                        )
        ) {

            throw new IllegalArgumentException(
                    "El logo debe enviarse optimizado en formato WebP."
            );
        }


        String fileName =
                "logo_"
                        +
                        UUID.randomUUID()
                        +
                        ".webp";


        Path target =
                resolve(
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
                    "No se pudo guardar el logo de la empresa.",
                    exception
            );
        }


        return fileName;
    }


    public Resource load(
            String fileName
    ) {

        Path path =
                resolve(
                        fileName
                );


        if (
                !Files.exists(path)
                        ||
                        !Files.isRegularFile(path)
        ) {

            throw new ResourceNotFoundException(
                    "El logo de la empresa no existe."
            );
        }


        try {

            return new UrlResource(
                    path.toUri()
            );

        } catch (Exception exception) {

            throw new FileStorageException(
                    "No se pudo leer el logo de la empresa.",
                    exception
            );
        }
    }


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
                    resolve(
                            fileName
                    )
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "No se pudo eliminar el logo anterior.",
                    exception
            );
        }
    }


    public String getContentType() {

        return "image/webp";
    }


    private Path resolve(
            String fileName
    ) {

        return pathService.resolveFile(

                currentTenantIdentityService
                        .getCurrentTenantId(),

                StorageArea.COMPANY,

                fileName
        );
    }
}