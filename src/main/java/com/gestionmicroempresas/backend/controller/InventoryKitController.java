package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.KitOperationRequest;

import com.gestionmicroempresas.backend.service.KitService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
public class InventoryKitController {

    private final KitService kitService;


    public InventoryKitController(
            KitService kitService
    ) {

        this.kitService =
                kitService;
    }


    // =====================================================
    // ASSEMBLE
    // =====================================================

    @PostMapping("/{id}/armar")
    public ResponseEntity<Void> assembleKit(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            KitOperationRequest request
    ) {

        kitService.assembleKit(

                getUserId(jwt),

                id,

                request
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // DISASSEMBLE
    // =====================================================

    @PostMapping("/{id}/desarmar")
    public ResponseEntity<Void> disassembleKit(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            KitOperationRequest request
    ) {

        kitService.disassembleKit(

                getUserId(jwt),

                id,

                request
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // USER ID
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