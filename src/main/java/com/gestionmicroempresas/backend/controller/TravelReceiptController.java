package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.service.TravelService;

import com.gestionmicroempresas.backend.storage.TravelReceiptStorageService;

import org.springframework.core.io.Resource;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/archivos/viaticos")
public class TravelReceiptController {

    private final TravelReceiptStorageService storageService;

    private final TravelService travelService;


    public TravelReceiptController(
            TravelReceiptStorageService storageService,
            TravelService travelService
    ) {

        this.storageService =
                storageService;

        this.travelService =
                travelService;
    }


    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getReceipt(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            String fileName
    ) {

        travelService.validateReceiptAccess(
                getUserId(jwt),
                fileName
        );


        Resource resource =
                storageService.loadReceipt(
                        fileName
                );


        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.noStore()
                )
                .contentType(
                        MediaType.parseMediaType(
                                storageService
                                        .getContentType(
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