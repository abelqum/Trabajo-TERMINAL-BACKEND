package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.ManualExpense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public interface ManualExpenseRepository
        extends JpaRepository<ManualExpense, Long> {

    Optional<ManualExpense> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    List<ManualExpense>
    findAllByTenant_IdAndExpenseDateBetweenOrderByExpenseDateDescIdDesc(
            Long tenantId,
            LocalDate dateFrom,
            LocalDate dateTo
    );


    List<ManualExpense> findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}