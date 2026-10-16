package com.gestionmicroempresas.backend.config;

import com.gestionmicroempresas.backend.entity.Tenant;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.Role;

import com.gestionmicroempresas.backend.repository.TenantRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.bootstrap-admin.enabled",
        havingValue = "true"
)
public class AdminBootstrap
        implements ApplicationRunner {

    private final UserRepository userRepository;

    private final TenantRepository tenantRepository;

    private final PasswordEncoder passwordEncoder;

    private final String name;

    private final String email;

    private final String password;


    public AdminBootstrap(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,

            @Value("${app.bootstrap-admin.name}")
            String name,

            @Value("${app.bootstrap-admin.email}")
            String email,

            @Value("${app.bootstrap-admin.password}")
            String password
    ) {

        this.userRepository =
                userRepository;

        this.tenantRepository =
                tenantRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.name =
                name;

        this.email =
                email;

        this.password =
                password;
    }


    @Override
    public void run(
            ApplicationArguments args
    ) {

        if (
                userRepository.count()
                        >
                        0
        ) {

            return;
        }


        if (
                email == null
                        ||
                        email.isBlank()
        ) {

            throw new IllegalStateException(
                    "APP_ADMIN_EMAIL es obligatorio cuando el bootstrap está habilitado."
            );
        }


        if (
                password == null
                        ||
                        password.isBlank()
        ) {

            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD es obligatorio cuando el bootstrap está habilitado."
            );
        }


        Tenant tenant =
                tenantRepository
                        .findFirstByOrderByIdAsc()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No existe el tenant inicial."
                                        )
                        );


        User administrator =
                new User(
                        tenant,
                        name,
                        email,

                        passwordEncoder.encode(
                                password
                        ),

                        Role.ADMIN
                );


        userRepository.saveAndFlush(
                administrator
        );
    }
}