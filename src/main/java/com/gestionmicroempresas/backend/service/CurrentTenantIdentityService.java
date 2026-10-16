package com.gestionmicroempresas.backend.service;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.stereotype.Service;

@Service
public class CurrentTenantIdentityService {

    public Long getCurrentTenantId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
        ) {

            throw new AccessDeniedException(
                    "No existe una sesión autenticada."
            );
        }


        if (
                !(
                        authentication.getPrincipal()
                                instanceof Jwt jwt
                )
        ) {

            throw new AccessDeniedException(
                    "La sesión autenticada no contiene información de empresa."
            );
        }


        Number tenantId =
                jwt.getClaim(
                        "tid"
                );


        if (
                tenantId == null
                        ||
                        tenantId.longValue() <= 0
        ) {

            throw new AccessDeniedException(
                    "La sesión no contiene una empresa válida."
            );
        }


        return tenantId.longValue();
    }
}