package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.UpdateCompanyBrandingRequest;

import com.gestionmicroempresas.backend.dto.response.TenantSummaryResponse;

import com.gestionmicroempresas.backend.service.CompanySettingsService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/configuracion/empresa")
public class CompanySettingsController {

    private final CompanySettingsService companySettingsService;


    public CompanySettingsController(
            CompanySettingsService companySettingsService
    ) {

        this.companySettingsService =
                companySettingsService;
    }


    @GetMapping
    public ResponseEntity<TenantSummaryResponse> getCompany() {

        return ResponseEntity.ok(

                companySettingsService
                        .getCompany()
        );
    }


    @PutMapping(
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantSummaryResponse> updateCompany(

            @AuthenticationPrincipal
            Jwt jwt,


            @Valid
            @RequestPart("datos")
            UpdateCompanyBrandingRequest request,


            @RequestPart(
                    value = "logo",
                    required = false
            )
            MultipartFile logo
    ) {

        return ResponseEntity.ok(

                companySettingsService
                        .updateCompany(
                                getUserId(jwt),
                                request,
                                logo
                        )
        );
    }


    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantSummaryResponse> deleteLogo(

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        return ResponseEntity.ok(

                companySettingsService
                        .deleteLogo(
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