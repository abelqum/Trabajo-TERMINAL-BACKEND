package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CatalogItemRequest;
import com.gestionmicroempresas.backend.dto.request.ChangePasswordRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateProfileRequest;

import com.gestionmicroempresas.backend.dto.response.CatalogItemResponse;
import com.gestionmicroempresas.backend.dto.response.ConfigurationProfileResponse;
import com.gestionmicroempresas.backend.dto.response.ConfigurationResponse;

import com.gestionmicroempresas.backend.service.ConfigurationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuracion")
public class ConfigurationController {

    private final ConfigurationService configurationService;


    public ConfigurationController(
            ConfigurationService configurationService
    ) {

        this.configurationService =
                configurationService;
    }


    // =====================================================
    // CONFIGURATION
    // =====================================================

    @GetMapping
    public ResponseEntity<ConfigurationResponse> getConfiguration(

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        return ResponseEntity.ok(

                configurationService
                        .getConfiguration(
                                getUserId(jwt)
                        )
        );
    }


    // =====================================================
    // PROFILE
    // =====================================================

    @PutMapping("/perfil")
    public ResponseEntity<ConfigurationProfileResponse> updateProfile(

            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestBody
            UpdateProfileRequest request
    ) {

        return ResponseEntity.ok(

                configurationService.updateProfile(

                        getUserId(jwt),

                        request
                )
        );
    }


    // =====================================================
    // PASSWORD
    // =====================================================

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(

            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestBody
            ChangePasswordRequest request
    ) {

        configurationService.changePassword(

                getUserId(jwt),

                request
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // GET CATALOG
    // =====================================================

    @GetMapping("/catalogos/{tipo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CatalogItemResponse>> getCatalog(

            @PathVariable(
                    name = "tipo"
            )
            String catalogType
    ) {

        return ResponseEntity.ok(

                configurationService.getCatalog(
                        catalogType
                )
        );
    }


    // =====================================================
    // CREATE CATALOG ITEM
    // =====================================================

    @PostMapping("/catalogos/{tipo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogItemResponse> createCatalogItem(

            @PathVariable(
                    name = "tipo"
            )
            String catalogType,

            @Valid
            @RequestBody
            CatalogItemRequest request
    ) {

        return ResponseEntity
                .status(201)
                .body(

                        configurationService
                                .createCatalogItem(

                                        catalogType,

                                        request
                                )
                );
    }


    // =====================================================
    // UPDATE CATALOG ITEM
    // =====================================================

    @PutMapping(
            "/catalogos/{tipo}/{id}"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogItemResponse> updateCatalogItem(

            @PathVariable(
                    name = "tipo"
            )
            String catalogType,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            CatalogItemRequest request
    ) {

        return ResponseEntity.ok(

                configurationService
                        .updateCatalogItem(

                                catalogType,

                                id,

                                request
                        )
        );
    }


    // =====================================================
    // DELETE CATALOG ITEM
    // =====================================================

    @DeleteMapping(
            "/catalogos/{tipo}/{id}"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCatalogItem(

            @PathVariable(
                    name = "tipo"
            )
            String catalogType,

            @PathVariable
            Long id
    ) {

        configurationService
                .deleteCatalogItem(

                        catalogType,

                        id
                );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // JWT USER ID
    // =====================================================

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