package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.ProblemReportRequest;

import com.gestionmicroempresas.backend.service.ProblemReportService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportar-problema")
public class ProblemReportController {

    private final ProblemReportService problemReportService;


    public ProblemReportController(
            ProblemReportService problemReportService
    ) {

        this.problemReportService =
                problemReportService;
    }


    // =====================================================
    // REPORT PROBLEM
    // =====================================================

    @PostMapping
    public ResponseEntity<Void> reportProblem(

            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestBody
            ProblemReportRequest request
    ) {

        problemReportService.sendReport(

                getUserId(
                        jwt
                ),

                request
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


        if (
                userId == null
        ) {

            throw new IllegalStateException(
                    "La sesión no contiene un usuario válido."
            );
        }


        return userId.longValue();
    }
}