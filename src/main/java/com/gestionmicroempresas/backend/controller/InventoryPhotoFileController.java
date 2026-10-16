package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.service.InventoryService;

import com.gestionmicroempresas.backend.storage.InventoryPhotoStorageService;

import org.springframework.core.io.Resource;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/archivos/inventario")
public class InventoryPhotoFileController {

    private final InventoryPhotoStorageService storageService;

    private final InventoryService inventoryService;


    public InventoryPhotoFileController(
            InventoryPhotoStorageService storageService,
            InventoryService inventoryService
    ) {

        this.storageService = storageService;
        this.inventoryService = inventoryService;
    }


    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getPhoto(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            String fileName
    ) {

        inventoryService.validatePhotoAccess(
                getUserId(jwt),
                fileName
        );


        Resource resource =
                storageService.load(
                        fileName
                );


        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.noStore()
                )
                .contentType(
                        MediaType.parseMediaType(

                                storageService.getContentType(
                                        fileName
                                )
                        )
                )
                .body(
                        resource
                );
    }


    private Long getUserId(
            Jwt jwt
    ) {

        Number userId =
                jwt.getClaim(
                        "uid"
                );


        if (userId == null) {

            throw new IllegalStateException(
                    "El token no contiene el identificador del usuario."
            );
        }


        return userId.longValue();
    }
}