package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CreateTravelRequest;
import com.gestionmicroempresas.backend.dto.request.TravelExpenseItemRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTravelExpenseStatusRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateTravelRequest;

import com.gestionmicroempresas.backend.dto.response.TravelExpenseResponse;
import com.gestionmicroempresas.backend.dto.response.TravelResponse;
import com.gestionmicroempresas.backend.dto.response.TravelsPageResponse;

import com.gestionmicroempresas.backend.service.TravelService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/viaticos")
public class TravelController {

    private final TravelService travelService;


    public TravelController(
            TravelService travelService
    ) {

        this.travelService =
                travelService;
    }


    // =====================================================
    // GET TRAVELS
    // =====================================================

    @GetMapping
    public ResponseEntity<TravelsPageResponse> getTravels(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "9"
            )
            int size,

            @RequestParam(
                    defaultValue = "fechaInicio,desc"
            )
            String sort
    ) {

        return ResponseEntity.ok(

                travelService.getTravels(

                        getUserId(jwt),

                        page,

                        size,

                        sort
                )
        );
    }


    // =====================================================
    // GET TRAVEL
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<TravelResponse> getTravel(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        return ResponseEntity.ok(

                travelService.getTravel(

                        getUserId(jwt),

                        id
                )
        );
    }


    // =====================================================
    // CREATE TRAVEL
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelResponse> createTravel(

            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestBody
            CreateTravelRequest request
    ) {

        TravelResponse travel =
                travelService.createTravel(

                        getUserId(jwt),

                        request
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        travel
                );
    }


    // =====================================================
    // UPDATE TRAVEL
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelResponse> updateTravel(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateTravelRequest request
    ) {

        return ResponseEntity.ok(

                travelService.updateTravel(

                        getUserId(jwt),

                        id,

                        request
                )
        );
    }


    // =====================================================
    // DELETE TRAVEL
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTravel(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        travelService.deleteTravel(

                getUserId(jwt),

                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // DELETE RECEIPTS
    // =====================================================

    @DeleteMapping("/{id}/comprobantes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReceipts(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        travelService.deleteReceipts(

                getUserId(jwt),

                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // REGISTER EXPENSES
    // =====================================================

    @PostMapping(
            value = "/{id}/gastos/lote",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<TravelResponse> registerExpenses(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @RequestPart("datos")
            List<@Valid TravelExpenseItemRequest> expenses,

            @RequestPart(
                    value = "comprobantes",
                    required = false
            )
            List<MultipartFile> receipts
    ) {

        TravelResponse travel =
                travelService.registerExpenses(

                        getUserId(jwt),

                        id,

                        expenses,

                        receipts
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        travel
                );
    }


    // =====================================================
    // DELETE EXPENSE
    // =====================================================

    @DeleteMapping(
            "/{id}/gastos/{gastoId}"
    )
    public ResponseEntity<Void> deleteExpense(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @PathVariable(
                    name = "gastoId"
            )
            Long expenseId
    ) {

        travelService.deleteExpense(

                getUserId(jwt),

                id,

                expenseId
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // REVIEW EXPENSE
    // =====================================================

    @PatchMapping(
            "/{id}/gastos/{gastoId}/estado"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelExpenseResponse> updateExpenseStatus(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @PathVariable(
                    name = "gastoId"
            )
            Long expenseId,

            @Valid
            @RequestBody
            UpdateTravelExpenseStatusRequest request
    ) {

        return ResponseEntity.ok(

                travelService.updateExpenseStatus(

                        getUserId(jwt),

                        id,

                        expenseId,

                        request
                )
        );
    }


    // =====================================================
    // FINISH TRAVEL
    // =====================================================

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelResponse> finishTravel(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        return ResponseEntity.ok(

                travelService.finishTravel(

                        getUserId(jwt),

                        id
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