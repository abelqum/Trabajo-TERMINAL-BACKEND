package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.InventoryStockMovement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryStockMovementRepository
        extends JpaRepository<InventoryStockMovement, Long> {

    boolean existsByTenant_IdAndProduct_Id(
            Long tenantId,
            Long productId
    );


    List<InventoryStockMovement>
    findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}