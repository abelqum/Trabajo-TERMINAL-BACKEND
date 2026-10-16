package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CreateIncomeRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateIncomeRequest;

import com.gestionmicroempresas.backend.dto.response.IncomeReportResponse;
import com.gestionmicroempresas.backend.dto.response.IncomeResponse;
import com.gestionmicroempresas.backend.dto.response.IncomeSummaryResponse;
import com.gestionmicroempresas.backend.dto.response.IncomesPageResponse;

import com.gestionmicroempresas.backend.service.IncomeService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ingresos")
public class IncomeController {

    private final IncomeService incomeService;


    public IncomeController(
            IncomeService incomeService
    ) {

        this.incomeService =
                incomeService;
    }


    // =====================================================
    // GET INCOMES
    // =====================================================

    @GetMapping
    public ResponseEntity<IncomesPageResponse> getIncomes(

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    defaultValue = "fecha,desc"
            )
            String sort,

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    name = "clienteId",
                    required = false
            )
            Long clientId,

            @RequestParam(
                    name = "tipoRegistro",
                    required = false
            )
            String recordType,

            @RequestParam(
                    name = "estado",
                    required = false
            )
            String status,

            @RequestParam(
                    name = "fechaDesde",
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateFrom,

            @RequestParam(
                    name = "fechaHasta",
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateTo,

            @RequestParam(
                    name = "fechaReferencia",
                    defaultValue = "REGISTRO"
            )
            String dateReference
    ) {

        return ResponseEntity.ok(

                incomeService.getIncomes(

                        page,
                        size,
                        sort,
                        search,
                        clientId,
                        recordType,
                        status,
                        dateFrom,
                        dateTo,
                        dateReference
                )
        );
    }


    // =====================================================
    // SUMMARY
    // =====================================================

    @GetMapping("/resumen")
    public ResponseEntity<IncomeSummaryResponse> getSummary(

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    name = "clienteId",
                    required = false
            )
            Long clientId,

            @RequestParam(
                    name = "tipoRegistro",
                    required = false
            )
            String recordType,

            @RequestParam(
                    name = "estado",
                    required = false
            )
            String status,

            @RequestParam(
                    name = "fechaDesde",
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateFrom,

            @RequestParam(
                    name = "fechaHasta",
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateTo,

            @RequestParam(
                    name = "fechaReferencia",
                    defaultValue = "REGISTRO"
            )
            String dateReference
    ) {

        return ResponseEntity.ok(

                incomeService.getSummary(

                        search,
                        clientId,
                        recordType,
                        status,
                        dateFrom,
                        dateTo,
                        dateReference
                )
        );
    }


    // =====================================================
    // REPORT
    // =====================================================

    @GetMapping("/reporte")
    public ResponseEntity<IncomeReportResponse> getReport(

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    name = "clienteId",
                    required = false
            )
            Long clientId,

            @RequestParam(
                    name = "tipoRegistro",
                    required = false
            )
            String recordType,

            @RequestParam(
                    name = "estado",
                    required = false
            )
            String status,

            @RequestParam(
                    name = "fechaDesde",
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateFrom,

            @RequestParam(
                    name = "fechaHasta",
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateTo,

            @RequestParam(
                    name = "fechaReferencia",
                    defaultValue = "REGISTRO"
            )
            String dateReference
    ) {

        return ResponseEntity.ok(

                incomeService.getReport(

                        search,
                        clientId,
                        recordType,
                        status,
                        dateFrom,
                        dateTo,
                        dateReference
                )
        );
    }


    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(

            @Valid
            @RequestBody
            CreateIncomeRequest request
    ) {

        IncomeResponse income =
                incomeService.createIncome(
                        request
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        income
                );
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateIncomeRequest request
    ) {

        return ResponseEntity.ok(

                incomeService.updateIncome(
                        id,
                        request
                )
        );
    }


    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(

            @PathVariable
            Long id
    ) {

        incomeService.deleteIncome(
                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}