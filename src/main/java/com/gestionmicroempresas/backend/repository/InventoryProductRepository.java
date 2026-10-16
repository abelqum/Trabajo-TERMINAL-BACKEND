package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.InventoryProduct;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryProductRepository
        extends JpaRepository<InventoryProduct, Long>,
        JpaSpecificationExecutor<InventoryProduct> {

    Optional<InventoryProduct>
    findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    Optional<InventoryProduct>
    findByTenant_IdAndQrCode(
            Long tenantId,
            String qrCode
    );


    Optional<InventoryProduct>
    findByTenant_IdAndPhotoFileName(
            Long tenantId,
            String photoFileName
    );


    List<InventoryProduct>
    findAllByTenant_IdAndKitTrueOrderByDescriptionAsc(
            Long tenantId
    );


    List<InventoryProduct>
    findAllByTenant_IdAndKitFalseOrderByDescriptionAsc(
            Long tenantId
    );


    List<InventoryProduct>
    findAllByTenant_IdAndIdIn(
            Long tenantId,
            Collection<Long> ids
    );


    List<InventoryProduct>
    findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT product
            FROM InventoryProduct product
            WHERE product.tenant.id = :tenantId
              AND product.id = :id
            """)
    Optional<InventoryProduct> findByTenantAndIdForUpdate(
            @Param("tenantId")
            Long tenantId,

            @Param("id")
            Long id
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT product
            FROM InventoryProduct product
            WHERE product.tenant.id = :tenantId
              AND product.id IN :ids
            ORDER BY product.id
            """)
    List<InventoryProduct> findAllByTenantAndIdsForUpdate(
            @Param("tenantId")
            Long tenantId,

            @Param("ids")
            Collection<Long> ids
    );
}