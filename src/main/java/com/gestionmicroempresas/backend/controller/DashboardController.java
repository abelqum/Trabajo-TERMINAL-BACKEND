package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.response.DashboardExpenseCategoriesResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardFinancialSummaryResponse;
import com.gestionmicroempresas.backend.dto.response.DashboardMonthlyResponse;

import com.gestionmicroempresas.backend.service.DashboardService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;


    public DashboardController(
            DashboardService dashboardService
    ) {

        this.dashboardService =
                dashboardService;
    }


    // =====================================================
    // HOME
    // =====================================================

    @GetMapping("/inicio")
    public ResponseEntity<Object> getHome(

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        return ResponseEntity.ok(

                dashboardService.getHome(
                        getUserId(jwt)
                )
        );
    }


    // =====================================================
    // MONTHLY
    // =====================================================

    @GetMapping("/financiero/mensual")
    public ResponseEntity<DashboardMonthlyResponse>
    getMonthlyEvolution(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam(
                    name = "anio"
            )
            int year
    ) {

        return ResponseEntity.ok(

                dashboardService
                        .getMonthlyEvolution(

                                getUserId(jwt),

                                year
                        )
        );
    }


    // =====================================================
    // PERIOD SUMMARY
    // =====================================================

    @GetMapping("/financiero/resumen")
    public ResponseEntity<DashboardFinancialSummaryResponse>
    getPeriodSummary(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam(
                    name = "desde"
            )
            String from,

            @RequestParam(
                    name = "hasta"
            )
            String to
    ) {

        return ResponseEntity.ok(

                dashboardService
                        .getPeriodSummary(

                                getUserId(jwt),

                                from,

                                to
                        )
        );
    }


    // =====================================================
    // EXPENSE CATEGORIES
    // =====================================================

    @GetMapping(
            "/financiero/categorias-egresos"
    )
    public ResponseEntity<DashboardExpenseCategoriesResponse>
    getExpenseCategories(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam(
                    name = "desde"
            )
            String from,

            @RequestParam(
                    name = "hasta"
            )
            String to
    ) {

        return ResponseEntity.ok(

                dashboardService
                        .getExpenseCategories(

                                getUserId(jwt),

                                from,

                                to
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