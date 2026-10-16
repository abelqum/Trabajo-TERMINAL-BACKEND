package com.gestionmicroempresas.backend.repository;

import com.gestionmicroempresas.backend.entity.Client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ClientRepository
        extends JpaRepository<Client, Long>,
        JpaSpecificationExecutor<Client> {

    Optional<Client> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );


    boolean existsByTenant_IdAndRfcIgnoreCase(
            Long tenantId,
            String rfc
    );


    boolean existsByTenant_IdAndRfcIgnoreCaseAndIdNot(
            Long tenantId,
            String rfc,
            Long id
    );


    List<Client> findAllByTenant_IdOrderByBusinessNameAsc(
            Long tenantId
    );


    List<Client> findAllByTenant_IdOrderByIdAsc(
            Long tenantId
    );
}