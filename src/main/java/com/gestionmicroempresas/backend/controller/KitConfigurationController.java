package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.KitRequest;

import com.gestionmicroempresas.backend.dto.response.KitAvailableProductResponse;
import com.gestionmicroempresas.backend.dto.response.KitResponse;

import com.gestionmicroempresas.backend.service.KitService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/configuracion/kits")
@PreAuthorize("hasRole('ADMIN')")
public class KitConfigurationController {

    private final KitService kitService;


    public KitConfigurationController(
            KitService kitService
    ) {

        this.kitService =
                kitService;
    }


    // =====================================================
    // GET KITS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<KitResponse>> getKits() {

        return ResponseEntity.ok(

                kitService.getKits()
        );
    }


    // =====================================================
    // AVAILABLE PRODUCTS
    // =====================================================

    @GetMapping("/productos-disponibles")
    public ResponseEntity<List<KitAvailableProductResponse>>
    getAvailableProducts() {

        return ResponseEntity.ok(

                kitService.getAvailableProducts()
        );
    }


    // =====================================================
    // CREATE KIT
    // =====================================================

    @PostMapping(
            consumes = "multipart/form-data"
    )
    public ResponseEntity<KitResponse> createKit(

            @Valid
            @RequestPart("datos")
            KitRequest request,

            @RequestPart(
                    value = "foto",
                    required = false
            )
            MultipartFile photo
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(

                        kitService.createKit(

                                request,

                                photo
                        )
                );
    }


    // =====================================================
    // UPDATE KIT
    // =====================================================

    @PutMapping(
            value = "/{id}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<KitResponse> updateKit(

            @PathVariable
            Long id,

            @Valid
            @RequestPart("datos")
            KitRequest request,

            @RequestPart(
                    value = "foto",
                    required = false
            )
            MultipartFile photo
    ) {

        return ResponseEntity.ok(

                kitService.updateKit(

                        id,

                        request,

                        photo
                )
        );
    }


    // =====================================================
    // DELETE KIT
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKit(

            @PathVariable
            Long id
    ) {

        kitService.deleteKit(
                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}