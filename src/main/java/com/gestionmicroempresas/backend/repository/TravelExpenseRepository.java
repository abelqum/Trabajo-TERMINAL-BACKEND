package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.TravelExpense;
import com.gestionmicroempresas.backend.enums.TravelExpenseStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public interface TravelExpenseRepository
        extends JpaRepository<TravelExpense, Long> {

    List<TravelExpense>
    findAllByTenant_IdAndTravel_IdOrderByExpenseDateDescIdDesc(
            Long tenantId,
            Long travelId
    );


    Optional<TravelExpense>
    findByIdAndTenant_IdAndTravel_Id(
            Long expenseId,
            Long tenantId,
            Long travelId
    );


    long countByTenant_IdAndTravel_IdAndStatus(
            Long tenantId,
            Long travelId,
            TravelExpenseStatus status
    );


    @EntityGraph(
            attributePaths = {
                    "travel"
            }
    )
    List<TravelExpense>
    findAllByTenant_IdAndStatusAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
            Long tenantId,
            TravelExpenseStatus status,
            LocalDate dateFrom,
            LocalDate dateTo
    );


    List<TravelExpense> findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );


    Optional<TravelExpense>
    findByTenant_IdAndReceiptFileName(
            Long tenantId,
            String receiptFileName
    );
}