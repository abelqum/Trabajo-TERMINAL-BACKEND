package com.gestionmicroempresas.backend.config;

import com.gestionmicroempresas.backend.security.AppUserDetailsService;
import com.gestionmicroempresas.backend.security.CookieBearerTokenResolver;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String frontendUrl;


    public SecurityConfig(
            @Value("${app.frontend-url}")
            String frontendUrl
    ) {

        this.frontendUrl =
                frontendUrl;
    }


    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =====================================================
    // AUTHENTICATION PROVIDER
    // =====================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );


        provider.setPasswordEncoder(
                passwordEncoder
        );


        return provider;
    }


    // =====================================================
    // AUTHENTICATION MANAGER
    // =====================================================

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider
    ) {

        return new ProviderManager(
                authenticationProvider
        );
    }


    // =====================================================
    // JWT ROLES
    // =====================================================

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();


        authoritiesConverter.setAuthoritiesClaimName(
                "roles"
        );


        authoritiesConverter.setAuthorityPrefix(
                "ROLE_"
        );


        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();


        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );


        return converter;
    }


    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            CookieBearerTokenResolver tokenResolver,
            JwtAuthenticationConverter jwtAuthenticationConverter
    )
            throws Exception {

        http
                // =========================================
                // CORS
                // =========================================

                .cors(
                        cors ->
                                cors.configurationSource(
                                        corsConfigurationSource()
                                )
                )


                // =========================================
                // CSRF
                // =========================================
                //
                // Spring Security 7 SPA mode:
                //
                // XSRF-TOKEN cookie
                // X-XSRF-TOKEN request header
                //
                // GET / HEAD / OPTIONS remain safe.
                // POST / PUT / PATCH / DELETE require token.
                // =========================================

                .csrf(
                        csrf ->
                                csrf.spa()
                )


                // =========================================
                // DISABLED LOGIN MECHANISMS
                // =========================================

                .formLogin(
                        AbstractHttpConfigurer::disable
                )

                .httpBasic(
                        AbstractHttpConfigurer::disable
                )


                // =========================================
                // STATELESS AUTH
                // =========================================

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )


                .authenticationProvider(
                        authenticationProvider
                )


                // =========================================
                // AUTHORIZATION
                // =========================================

                .authorizeHttpRequests(
                        auth ->
                                auth

                                        // =================
                                        // CSRF BOOTSTRAP
                                        // =================

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/auth/csrf"
                                        )
                                        .permitAll()


                                        // =================
                                        // PUBLIC AUTH
                                        // =================

                                        .requestMatchers(
                                                HttpMethod.POST,

                                                "/api/auth/login",

                                                "/api/auth/registro",

                                                "/api/auth/logout"
                                        )
                                        .permitAll()


                                        // =================
                                        // PREFLIGHT
                                        // =================

                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()


                                        .requestMatchers(
                                                "/error"
                                        )
                                        .permitAll()


                                        // =================
                                        // EVERYTHING ELSE
                                        // =================

                                        .anyRequest()
                                        .authenticated()
                )


                // =========================================
                // JWT RESOURCE SERVER
                // =========================================

                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2

                                        .bearerTokenResolver(
                                                tokenResolver
                                        )

                                        .jwt(
                                                jwt ->
                                                        jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter
                                                        )
                                        )
                );


        return http.build();
    }


    // =====================================================
    // CORS
    // =====================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        configuration.setAllowedOrigins(
                List.of(
                        frontendUrl
                )
        );


        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );


        configuration.setAllowedHeaders(
                List.of(
                        "Content-Type",
                        "Accept",
                        "Authorization",
                        "X-XSRF-TOKEN"
                )
        );


        configuration.setAllowCredentials(
                true
        );


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }
}