package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.LoginRequest;
import com.gestionmicroempresas.backend.dto.request.RegisterTenantRequest;

import com.gestionmicroempresas.backend.dto.response.UserResponse;

import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.security.JwtService;

import com.gestionmicroempresas.backend.service.AuthRateLimitService;
import com.gestionmicroempresas.backend.service.AuthService;
import com.gestionmicroempresas.backend.service.RegistrationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.web.csrf.CsrfToken;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final RegistrationService registrationService;

    private final AuthRateLimitService authRateLimitService;

    private final JwtService jwtService;

    private final String cookieName;

    private final boolean cookieSecure;

    private final long expirationMinutes;


    public AuthController(
            AuthService authService,
            RegistrationService registrationService,
            AuthRateLimitService authRateLimitService,
            JwtService jwtService,

            @Value("${app.auth.cookie-name}")
            String cookieName,

            @Value("${app.auth.cookie-secure}")
            boolean cookieSecure,

            @Value("${app.jwt.expiration-minutes}")
            long expirationMinutes
    ) {

        this.authService =
                authService;

        this.registrationService =
                registrationService;

        this.authRateLimitService =
                authRateLimitService;

        this.jwtService =
                jwtService;

        this.cookieName =
                cookieName;

        this.cookieSecure =
                cookieSecure;

        this.expirationMinutes =
                expirationMinutes;
    }


    // =====================================================
    // CSRF
    // =====================================================

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(
            CsrfToken csrfToken
    ) {

        /*
         * Accessing the token forces Spring Security
         * to materialize it and send the XSRF-TOKEN
         * cookie to the browser.
         */
        csrfToken.getToken();


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // REGISTER TENANT
    // =====================================================

    @PostMapping("/registro")
    public ResponseEntity<UserResponse> register(

            @Valid
            @RequestBody
            RegisterTenantRequest request,

            HttpServletRequest httpRequest,

            HttpServletResponse response
    ) {

        authRateLimitService
                .checkRegistration(
                        httpRequest
                );


        User user =
                registrationService.register(
                        request
                );


        String token =
                jwtService.generateToken(
                        user
                );


        addAuthenticationCookie(
                response,
                token
        );


        return ResponseEntity
                .status(201)
                .body(
                        UserResponse.from(
                                user
                        )
                );
    }


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(

            @Valid
            @RequestBody
            LoginRequest request,

            HttpServletRequest httpRequest,

            HttpServletResponse response
    ) {

        authRateLimitService
                .checkLogin(
                        httpRequest,
                        request.email()
                );


        User user =
                authService.authenticate(
                        request
                );


        authRateLimitService
                .registerLoginSuccess(
                        httpRequest,
                        request.email()
                );


        String token =
                jwtService.generateToken(
                        user
                );


        addAuthenticationCookie(
                response,
                token
        );


        return ResponseEntity.ok(
                UserResponse.from(
                        user
                )
        );
    }


    // =====================================================
    // CURRENT USER
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        Number userId =
                jwt.getClaim(
                        "uid"
                );


        Number tenantId =
                jwt.getClaim(
                        "tid"
                );


        if (
                userId == null
                        ||
                        tenantId == null
        ) {

            throw new IllegalStateException(
                    "El token no contiene la información de sesión requerida."
            );
        }


        User user =
                authService.getById(
                        userId.longValue(),
                        tenantId.longValue()
                );


        return ResponseEntity.ok(

                UserResponse.from(
                        user
                )
        );
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletResponse response
    ) {

        ResponseCookie cookie =
                ResponseCookie
                        .from(
                                cookieName,
                                ""
                        )

                        .httpOnly(true)

                        .secure(
                                cookieSecure
                        )

                        .sameSite(
                                "Lax"
                        )

                        .path("/")

                        .maxAge(
                                Duration.ZERO
                        )

                        .build();


        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // AUTH COOKIE
    // =====================================================

    private void addAuthenticationCookie(
            HttpServletResponse response,
            String token
    ) {

        ResponseCookie cookie =
                ResponseCookie
                        .from(
                                cookieName,
                                token
                        )

                        .httpOnly(true)

                        .secure(
                                cookieSecure
                        )

                        .sameSite(
                                "Lax"
                        )

                        .path("/")

                        .maxAge(
                                Duration.ofMinutes(
                                        expirationMinutes
                                )
                        )

                        .build();


        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}