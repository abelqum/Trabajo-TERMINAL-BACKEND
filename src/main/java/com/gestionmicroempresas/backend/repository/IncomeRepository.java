package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.Income;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface IncomeRepository
        extends JpaRepository<Income, Long>,
        JpaSpecificationExecutor<Income> {

    Optional<Income> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    List<Income> findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}