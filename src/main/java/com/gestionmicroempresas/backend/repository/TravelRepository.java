package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.Travel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelRepository
        extends JpaRepository<Travel, Long> {

    Optional<Travel> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    Page<Travel> findAllByTenant_Id(
            Long tenantId,
            Pageable pageable
    );


    Page<Travel>
    findDistinctByTenant_IdAndAssignedUsers_Id(
            Long tenantId,
            Long userId,
            Pageable pageable
    );


    List<Travel> findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}