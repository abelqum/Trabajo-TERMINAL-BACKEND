package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.service.CompanySettingsService;

import com.gestionmicroempresas.backend.storage.CompanyLogoStorageService;

import org.springframework.core.io.Resource;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/archivos/empresa")
public class CompanyLogoFileController {

    private final CompanySettingsService companySettingsService;

    private final CompanyLogoStorageService logoStorageService;


    public CompanyLogoFileController(
            CompanySettingsService companySettingsService,
            CompanyLogoStorageService logoStorageService
    ) {

        this.companySettingsService =
                companySettingsService;

        this.logoStorageService =
                logoStorageService;
    }


    @GetMapping("/logo")
    public ResponseEntity<Resource> getLogo() {

        String fileName =
                companySettingsService
                        .getCurrentLogoFileName();


        Resource resource =
                logoStorageService.load(
                        fileName
                );


        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.noStore()
                )
                .contentType(
                        MediaType.parseMediaType(
                                logoStorageService
                                        .getContentType()
                        )
                )
                .body(
                        resource
                );
    }
}