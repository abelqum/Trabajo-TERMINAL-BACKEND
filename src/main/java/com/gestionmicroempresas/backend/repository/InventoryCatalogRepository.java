package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;
import com.gestionmicroempresas.backend.enums.InventoryCatalogType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryCatalogRepository
        extends JpaRepository<InventoryCatalogItem, Long> {

    List<InventoryCatalogItem>
    findAllByTenant_IdAndTypeOrderByNameAsc(
            Long tenantId,
            InventoryCatalogType type
    );


    List<InventoryCatalogItem>
    findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );


    Optional<InventoryCatalogItem>
    findByIdAndTenant_IdAndType(
            Long id,
            Long tenantId,
            InventoryCatalogType type
    );


    boolean existsByTenant_IdAndTypeAndNameIgnoreCase(
            Long tenantId,
            InventoryCatalogType type,
            String name
    );


    boolean existsByTenant_IdAndTypeAndNameIgnoreCaseAndIdNot(
            Long tenantId,
            InventoryCatalogType type,
            String name,
            Long id
    );
}