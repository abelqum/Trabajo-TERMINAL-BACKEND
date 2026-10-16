package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CreateInventoryProductRequest;
import com.gestionmicroempresas.backend.dto.request.DeleteInventoryProductsRequest;
import com.gestionmicroempresas.backend.dto.request.StockAdjustmentRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateInventoryProductRequest;

import com.gestionmicroempresas.backend.dto.response.InventoryCatalogsResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryPageResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryProductResponse;
import com.gestionmicroempresas.backend.dto.response.InventoryReportResponse;

import com.gestionmicroempresas.backend.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/inventario")
public class InventoryController {

    private final InventoryService inventoryService;


    public InventoryController(
            InventoryService inventoryService
    ) {

        this.inventoryService =
                inventoryService;
    }


    // =====================================================
    // INVENTORY
    // =====================================================

    @GetMapping
    public ResponseEntity<InventoryPageResponse> getInventory(

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "15"
            )
            int size,

            @RequestParam(
                    defaultValue = "descripcion,asc"
            )
            String sort,

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    required = false
            )
            Long marcaId,

            @RequestParam(
                    required = false
            )
            Long categoriaId,

            @RequestParam(
                    required = false
            )
            Long medidaId,

            @RequestParam(
                    required = false
            )
            Long almacenId,

            @RequestParam(
                    required = false
            )
            String estatus
    ) {

        return ResponseEntity.ok(

                inventoryService.getInventory(

                        page,

                        size,

                        sort,

                        search,

                        marcaId,

                        categoriaId,

                        medidaId,

                        almacenId,

                        estatus
                )
        );
    }


    // =====================================================
    // CATALOGS
    // =====================================================

    @GetMapping("/catalogos")
    public ResponseEntity<InventoryCatalogsResponse> getCatalogs() {

        return ResponseEntity.ok(

                inventoryService.getCatalogs()
        );
    }


    // =====================================================
    // QR
    // =====================================================

    @GetMapping("/qr")
    public ResponseEntity<InventoryProductResponse> getProductByQr(

            @RequestParam(
                    name = "codigo"
            )
            String qrCode
    ) {

        return ResponseEntity.ok(

                inventoryService.getProductByQr(
                        qrCode
                )
        );
    }


    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping(
            consumes = "multipart/form-data"
    )
    public ResponseEntity<InventoryProductResponse> createProduct(

            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestPart("datos")
            CreateInventoryProductRequest request,

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

                        inventoryService.createProduct(

                                getUserId(jwt),

                                request,

                                photo
                        )
                );
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @PutMapping(
            value = "/{id}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<InventoryProductResponse> updateProduct(

            @PathVariable
            Long id,

            @Valid
            @RequestPart("datos")
            UpdateInventoryProductRequest request,

            @RequestPart(
                    value = "foto",
                    required = false
            )
            MultipartFile photo
    ) {

        return ResponseEntity.ok(

                inventoryService.updateProduct(

                        id,

                        request,

                        photo
                )
        );
    }


    // =====================================================
    // DELETE ONE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(

            @PathVariable
            Long id
    ) {

        inventoryService.deleteProduct(
                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // DELETE BATCH
    // =====================================================

    @DeleteMapping("/lote")
    public ResponseEntity<Void> deleteProducts(

            @Valid
            @RequestBody
            DeleteInventoryProductsRequest request
    ) {

        inventoryService.deleteProducts(
                request
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // DUPLICATE
    // =====================================================

    @PostMapping("/{id}/duplicar")
    public ResponseEntity<InventoryProductResponse> duplicateProduct(

            @PathVariable
            Long id
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(

                        inventoryService.duplicateProduct(
                                id
                        )
                );
    }


    // =====================================================
    // STOCK ADJUSTMENT
    // =====================================================

    @PostMapping("/{id}/ajustes")
    public ResponseEntity<InventoryProductResponse> adjustStock(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            StockAdjustmentRequest request
    ) {

        return ResponseEntity.ok(

                inventoryService.adjustStock(

                        getUserId(jwt),

                        id,

                        request
                )
        );
    }


    // =====================================================
    // REPORT
    // =====================================================

    @GetMapping("/reporte")
    public ResponseEntity<InventoryReportResponse> getReport(

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    required = false
            )
            Long marcaId,

            @RequestParam(
                    required = false
            )
            Long categoriaId,

            @RequestParam(
                    required = false
            )
            Long medidaId,

            @RequestParam(
                    required = false
            )
            Long almacenId,

            @RequestParam(
                    required = false
            )
            String estatus
    ) {

        return ResponseEntity.ok(

                inventoryService.getReport(

                        search,

                        marcaId,

                        categoriaId,

                        medidaId,

                        almacenId,

                        estatus
                )
        );
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