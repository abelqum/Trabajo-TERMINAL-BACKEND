package com.gestionmicroempresas.backend.service;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseTenantContextService {

    private final EntityManager entityManager;


    public DatabaseTenantContextService(
            EntityManager entityManager
    ) {

        this.entityManager =
                entityManager;
    }


    @Transactional(
            propagation = Propagation.MANDATORY
    )
    public void activate(
            Long tenantId
    ) {

        if (
                tenantId == null
                        ||
                        tenantId <= 0
        ) {

            throw new IllegalArgumentException(
                    "El identificador de la empresa no es válido."
            );
        }


        entityManager
                .createNativeQuery(
                        """
                        SELECT set_config(
                            'app.current_tenant',
                            :tenantId,
                            TRUE
                        )
                        """
                )
                .setParameter(
                        "tenantId",
                        tenantId.toString()
                )
                .getSingleResult();
    }
}