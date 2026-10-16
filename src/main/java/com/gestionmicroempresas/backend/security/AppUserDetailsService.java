package com.gestionmicroempresas.backend.security;

import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.TenantStatus;

import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;


    public AppUserDetailsService(
            UserRepository userRepository
    ) {

        this.userRepository =
                userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(
            String email
    )
            throws UsernameNotFoundException {

        User user =
                userRepository
                        .findByEmailIgnoreCase(
                                email
                        )
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Usuario no encontrado."
                                        )
                        );


        boolean tenantSuspended =
                user
                        .getTenant()
                        .getStatus()
                        ==
                        TenantStatus.SUSPENDED;


        return org.springframework.security
                .core
                .userdetails
                .User

                .withUsername(
                        user.getEmail()
                )

                .password(
                        user.getPasswordHash()
                )

                .authorities(
                        "ROLE_"
                                +
                                user
                                        .getRole()
                                        .name()
                )

                .disabled(
                        tenantSuspended
                )

                .build();
    }
}