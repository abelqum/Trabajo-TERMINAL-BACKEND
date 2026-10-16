package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.RegisterTenantRequest;

import com.gestionmicroempresas.backend.entity.Tenant;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.Role;

import com.gestionmicroempresas.backend.exception.ConflictException;

import com.gestionmicroempresas.backend.repository.TenantRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private final TenantRepository tenantRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public RegistrationService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.tenantRepository =
                tenantRepository;

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
    }


    // =====================================================
    // REGISTER TENANT
    // =====================================================

    @Transactional
    public User register(
            RegisterTenantRequest request
    ) {

        String companyName =
                normalizeRequiredText(
                        request.companyName(),
                        "El nombre de la empresa es obligatorio."
                );


        String name =
                normalizeRequiredText(
                        request.name(),
                        "Tu nombre es obligatorio."
                );


        String email =
                normalizeEmail(
                        request.email()
                );


        validatePasswords(
                request.password(),
                request.confirmPassword()
        );


        // =================================================
        // GLOBAL EMAIL
        // =================================================
        //
        // Current model:
        //
        // One user belongs to exactly one tenant.
        //
        // Therefore the email remains globally unique and
        // login can continue using only email + password.
        // =================================================

        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
        ) {

            throw new ConflictException(
                    "Ya existe una cuenta registrada con ese correo electrónico."
            );
        }


        // =================================================
        // TENANT
        // =================================================

        Tenant tenant =
                new Tenant(
                        companyName
                );


        tenant =
                tenantRepository.saveAndFlush(
                        tenant
                );


        // =================================================
        // FIRST ADMINISTRATOR
        // =================================================

        User administrator =
                new User(
                        tenant,
                        name,
                        email,

                        passwordEncoder.encode(
                                request.password()
                        ),

                        Role.ADMIN
                );


        return userRepository.saveAndFlush(
                administrator
        );
    }


    // =====================================================
    // PASSWORD
    // =====================================================

    private void validatePasswords(
            String password,
            String confirmation
    ) {

        if (
                password == null
                        ||
                        confirmation == null
        ) {

            throw new IllegalArgumentException(
                    "La contraseña y su confirmación son obligatorias."
            );
        }


        if (
                !password.equals(
                        confirmation
                )
        ) {

            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden."
            );
        }
    }


    // =====================================================
    // NORMALIZATION
    // =====================================================

    private String normalizeEmail(
            String email
    ) {

        if (
                email == null
                        ||
                        email.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El correo es obligatorio."
            );
        }


        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private String normalizeRequiredText(
            String value,
            String message
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    message
            );
        }


        return value.trim();
    }
}