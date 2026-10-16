package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.entity.Tenant;

import com.gestionmicroempresas.backend.enums.TenantStatus;

import com.gestionmicroempresas.backend.repository.TenantRepository;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentTenantService {

    private final TenantRepository tenantRepository;

    private final DatabaseTenantContextService databaseTenantContextService;

    private final CurrentTenantIdentityService currentTenantIdentityService;


    public CurrentTenantService(
            TenantRepository tenantRepository,
            DatabaseTenantContextService databaseTenantContextService,
            CurrentTenantIdentityService currentTenantIdentityService
    ) {

        this.tenantRepository =
                tenantRepository;

        this.databaseTenantContextService =
                databaseTenantContextService;

        this.currentTenantIdentityService =
                currentTenantIdentityService;
    }


    @Transactional(
            propagation = Propagation.MANDATORY,
            readOnly = true
    )
    public Long getCurrentTenantId() {

        return resolveCurrentTenant()
                .getId();
    }


    @Transactional(
            propagation = Propagation.MANDATORY,
            readOnly = true
    )
    public Tenant getCurrentTenant() {

        return resolveCurrentTenant();
    }


    private Tenant resolveCurrentTenant() {

        Long tenantId =
                currentTenantIdentityService
                        .getCurrentTenantId();


        databaseTenantContextService.activate(
                tenantId
        );


        Tenant tenant =
                tenantRepository
                        .findById(
                                tenantId
                        )
                        .orElseThrow(
                                () ->
                                        new AccessDeniedException(
                                                "La empresa asociada a la sesión no existe."
                                        )
                        );


        if (
                tenant.getStatus()
                        !=
                        TenantStatus.ACTIVE
        ) {

            throw new AccessDeniedException(
                    "La empresa asociada a la sesión no está activa."
            );
        }


        return tenant;
    }
}