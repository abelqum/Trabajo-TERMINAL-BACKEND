package com.gestionmicroempresas.backend.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TenantOwnedEntity {

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            updatable = false
    )
    private Tenant tenant;


    protected TenantOwnedEntity() {
    }


    protected TenantOwnedEntity(
            Tenant tenant
    ) {

        if (tenant == null) {

            throw new IllegalArgumentException(
                    "La empresa asociada al registro es obligatoria."
            );
        }


        this.tenant =
                tenant;
    }


    public Tenant getTenant() {
        return tenant;
    }


    public Long getTenantId() {

        return tenant == null
                ?
                null
                :
                tenant.getId();
    }
}