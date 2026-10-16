package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.response.SystemBackupResponse;

import com.gestionmicroempresas.backend.service.SystemBackupService;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracion/respaldo")
@PreAuthorize("hasRole('ADMIN')")
public class SystemBackupController {

    private final SystemBackupService systemBackupService;


    public SystemBackupController(
            SystemBackupService systemBackupService
    ) {

        this.systemBackupService =
                systemBackupService;
    }


    @GetMapping
    public ResponseEntity<SystemBackupResponse> downloadBackup(

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.noStore()
                )
                .body(
                        systemBackupService.generateBackup(
                                getUserId(jwt)
                        )
                );
    }


    private Long getUserId(
            Jwt jwt
    ) {

        Number userId =
                jwt.getClaim(
                        "uid"
                );


        if (userId == null) {

            throw new IllegalStateException(
                    "El token no contiene el identificador del usuario."
            );
        }


        return userId.longValue();
    }
}