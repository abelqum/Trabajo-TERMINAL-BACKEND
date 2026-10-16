package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.User;
import com.gestionmicroempresas.backend.enums.Role;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    // =====================================================
    // GLOBAL AUTHENTICATION
    // =====================================================

    Optional<User> findByEmailIgnoreCase(
            String email
    );


    boolean existsByEmailIgnoreCase(
            String email
    );


    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );


    // =====================================================
    // TENANT-AWARE
    // =====================================================

    Optional<User> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    boolean existsByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    List<User> findAllByTenant_IdAndIdIn(
            Long tenantId,
            Collection<Long> ids
    );


    long countByTenant_IdAndRole(
            Long tenantId,
            Role role
    );


    List<User> findAllByTenant_IdOrderByNameAsc(
            Long tenantId
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT user
            FROM User user
            WHERE user.tenant.id = :tenantId
              AND user.role = :role
            ORDER BY user.id
            """)
    List<User> findAllByTenantAndRoleForUpdate(
            @Param("tenantId")
            Long tenantId,

            @Param("role")
            Role role
    );
}