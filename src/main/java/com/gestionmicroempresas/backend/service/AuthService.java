package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.LoginRequest;

import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;


    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository
    ) {

        this.authenticationManager =
                authenticationManager;

        this.userRepository =
                userRepository;
    }


    public User authenticate(
            LoginRequest request
    ) {

        String email =
                request
                        .email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        email,

                        request.password()
                )
        );


        return userRepository
                .findByEmailIgnoreCase(
                        email
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no existe."
                                )
                );
    }


    public User getById(
            Long id,
            Long tenantId
    ) {

        return userRepository
                .findByIdAndTenant_Id(
                        id,
                        tenantId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no existe."
                                )
                );
    }
}