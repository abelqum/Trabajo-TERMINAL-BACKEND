package com.gestionmicroempresas.backend.storage;

import com.gestionmicroempresas.backend.exception.FileStorageException;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TenantStoragePathService {

    private final Path baseDirectory;

    private final Path tenantsDirectory;


    public TenantStoragePathService(

            @Value("${app.storage.base-directory}")
            String baseDirectory
    ) {

        this.baseDirectory =
                Path
                        .of(
                                baseDirectory
                        )
                        .toAbsolutePath()
                        .normalize();


        this.tenantsDirectory =
                this.baseDirectory
                        .resolve(
                                "tenants"
                        )
                        .normalize();
    }


    @PostConstruct
    public void initialize() {

        try {

            Files.createDirectories(
                    tenantsDirectory
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "No se pudo preparar el almacenamiento multiempresa.",
                    exception
            );
        }
    }


    public Path getTenantDirectory(
            Long tenantId,
            StorageArea area
    ) {

        validateTenantId(
                tenantId
        );


        if (area == null) {

            throw new IllegalArgumentException(
                    "El área de almacenamiento es obligatoria."
            );
        }


        Path tenantDirectory =
                tenantsDirectory
                        .resolve(
                                tenantId.toString()
                        )
                        .resolve(
                                area.getDirectoryName()
                        )
                        .normalize();


        if (
                !tenantDirectory.startsWith(
                        tenantsDirectory
                )
        ) {

            throw new IllegalArgumentException(
                    "La ruta de almacenamiento no es válida."
            );
        }


        try {

            Files.createDirectories(
                    tenantDirectory
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "No se pudo preparar el directorio de almacenamiento de la empresa.",
                    exception
            );
        }


        return tenantDirectory;
    }


    public Path resolveFile(
            Long tenantId,
            StorageArea area,
            String fileName
    ) {

        validateFileName(
                fileName
        );


        Path directory =
                getTenantDirectory(
                        tenantId,
                        area
                );


        Path file =
                directory
                        .resolve(
                                fileName
                        )
                        .normalize();


        if (
                !file.startsWith(
                        directory
                )
        ) {

            throw new IllegalArgumentException(
                    "El nombre del archivo no es válido."
            );
        }


        return file;
    }


    public Path getLegacyDirectory(
            StorageArea area
    ) {

        if (area == null) {

            throw new IllegalArgumentException(
                    "El área de almacenamiento es obligatoria."
            );
        }


        Path directory =
                baseDirectory
                        .resolve(
                                area.getDirectoryName()
                        )
                        .normalize();


        if (
                !directory.startsWith(
                        baseDirectory
                )
        ) {

            throw new IllegalArgumentException(
                    "La ruta de almacenamiento legado no es válida."
            );
        }


        return directory;
    }


    private void validateTenantId(
            Long tenantId
    ) {

        if (
                tenantId == null
                        ||
                        tenantId <= 0
        ) {

            throw new IllegalArgumentException(
                    "El identificador de la empresa no es válido."
            );
        }
    }


    private void validateFileName(
            String fileName
    ) {

        if (
                fileName == null
                        ||
                        fileName.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El nombre del archivo no es válido."
            );
        }


        String normalized =
                fileName.trim();


        if (
                normalized.equals(".")
                        ||
                        normalized.equals("..")
                        ||
                        normalized.contains("/")
                        ||
                        normalized.contains("\\")
        ) {

            throw new IllegalArgumentException(
                    "El nombre del archivo no es válido."
            );
        }
    }
}