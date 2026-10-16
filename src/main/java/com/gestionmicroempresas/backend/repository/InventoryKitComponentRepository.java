package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.InventoryKitComponent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryKitComponentRepository
        extends JpaRepository<InventoryKitComponent, Long> {

    List<InventoryKitComponent>
    findAllByTenant_IdAndKit_IdOrderByIdAsc(
            Long tenantId,
            Long kitId
    );


    void deleteAllByTenant_IdAndKit_Id(
            Long tenantId,
            Long kitId
    );


    boolean existsByTenant_IdAndComponent_Id(
            Long tenantId,
            Long productId
    );


    List<InventoryKitComponent>
    findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}