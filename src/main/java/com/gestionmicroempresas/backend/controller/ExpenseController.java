package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CreateExpenseRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateExpenseRequest;

import com.gestionmicroempresas.backend.dto.response.ExpenseResponse;
import com.gestionmicroempresas.backend.dto.response.ExpensesResponse;

import com.gestionmicroempresas.backend.service.ExpenseService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/egresos")
public class ExpenseController {

    private final ExpenseService expenseService;


    public ExpenseController(
            ExpenseService expenseService
    ) {

        this.expenseService =
                expenseService;
    }


    // =====================================================
    // GET EXPENSES
    // =====================================================

    @GetMapping
    public ResponseEntity<ExpensesResponse> getExpenses(

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

                expenseService.getExpenses(
                        from,
                        to
                )
        );
    }


    // =====================================================
    // CREATE EXPENSE
    // =====================================================

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(

            @Valid
            @RequestBody
            CreateExpenseRequest request
    ) {

        ExpenseResponse expense =
                expenseService.createExpense(
                        request
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        expense
                );
    }


    // =====================================================
    // UPDATE EXPENSE
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateExpenseRequest request
    ) {

        return ResponseEntity.ok(

                expenseService.updateExpense(
                        id,
                        request
                )
        );
    }


    // =====================================================
    // DELETE EXPENSE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(

            @PathVariable
            Long id
    ) {

        expenseService.deleteExpense(
                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}