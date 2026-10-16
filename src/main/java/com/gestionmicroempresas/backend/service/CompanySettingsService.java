package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.UpdateCompanyBrandingRequest;

import com.gestionmicroempresas.backend.dto.response.TenantSummaryResponse;

import com.gestionmicroempresas.backend.entity.Tenant;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.Role;

import com.gestionmicroempresas.backend.repository.TenantRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import com.gestionmicroempresas.backend.storage.CompanyLogoStorageService;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.web.multipart.MultipartFile;

@Service
public class CompanySettingsService {

    private final TenantRepository tenantRepository;

    private final UserRepository userRepository;

    private final CurrentTenantService currentTenantService;

    private final CompanyLogoStorageService logoStorageService;


    public CompanySettingsService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            CurrentTenantService currentTenantService,
            CompanyLogoStorageService logoStorageService
    ) {

        this.tenantRepository =
                tenantRepository;

        this.userRepository =
                userRepository;

        this.currentTenantService =
                currentTenantService;

        this.logoStorageService =
                logoStorageService;
    }


    // =====================================================
    // GET
    // =====================================================

    @Transactional(readOnly = true)
    public TenantSummaryResponse getCompany() {

        return TenantSummaryResponse.from(

                currentTenantService
                        .getCurrentTenant()
        );
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @Transactional
    public TenantSummaryResponse updateCompany(
            Long currentUserId,
            UpdateCompanyBrandingRequest request,
            MultipartFile logo
    ) {

        Tenant tenant =
                currentTenantService
                        .getCurrentTenant();


        validateAdministrator(
                currentUserId,
                tenant.getId()
        );


        String previousLogo =
                tenant.getLogoFileName();


        String newLogo =
                null;


        try {

            if (
                    logo != null
                            &&
                            !logo.isEmpty()
            ) {

                newLogo =
                        logoStorageService.store(
                                logo
                        );


                tenant.setLogoFileName(
                        newLogo
                );
            }


            tenant.setName(
                    request
                            .name()
                            .trim()
            );


            tenant.setPrimaryColor(
                    request.primaryColor()
            );


            tenant.setSidebarColor(
                    request.sidebarColor()
            );


            Tenant updatedTenant =
                    tenantRepository.saveAndFlush(
                            tenant
                    );


            if (
                    newLogo != null
                            &&
                            previousLogo != null
                            &&
                            !previousLogo.isBlank()
                            &&
                            !previousLogo.equals(
                                    newLogo
                            )
            ) {

                deleteAfterCommit(
                        previousLogo
                );
            }


            return TenantSummaryResponse.from(
                    updatedTenant
            );

        } catch (RuntimeException exception) {

            if (newLogo != null) {

                try {

                    logoStorageService.delete(
                            newLogo
                    );

                } catch (RuntimeException ignored) {
                }
            }


            throw exception;
        }
    }


    // =====================================================
    // DELETE LOGO
    // =====================================================

    @Transactional
    public TenantSummaryResponse deleteLogo(
            Long currentUserId
    ) {

        Tenant tenant =
                currentTenantService
                        .getCurrentTenant();


        validateAdministrator(
                currentUserId,
                tenant.getId()
        );


        String previousLogo =
                tenant.getLogoFileName();


        tenant.setLogoFileName(
                null
        );


        Tenant updatedTenant =
                tenantRepository.saveAndFlush(
                        tenant
                );


        if (
                previousLogo != null
                        &&
                        !previousLogo.isBlank()
        ) {

            deleteAfterCommit(
                    previousLogo
            );
        }


        return TenantSummaryResponse.from(
                updatedTenant
        );
    }


    // =====================================================
    // LOGO FILE
    // =====================================================

    @Transactional(readOnly = true)
    public String getCurrentLogoFileName() {

        Tenant tenant =
                currentTenantService
                        .getCurrentTenant();


        String fileName =
                tenant.getLogoFileName();


        if (
                fileName == null
                        ||
                        fileName.isBlank()
        ) {

            throw new com.gestionmicroempresas.backend.exception.ResourceNotFoundException(
                    "La empresa no tiene un logo configurado."
            );
        }


        return fileName;
    }


    // =====================================================
    // ADMIN
    // =====================================================

    private void validateAdministrator(
            Long currentUserId,
            Long tenantId
    ) {

        User user =
                userRepository
                        .findByIdAndTenant_Id(
                                currentUserId,
                                tenantId
                        )
                        .orElseThrow(
                                () ->
                                        new AccessDeniedException(
                                                "La sesión no corresponde a un usuario válido."
                                        )
                        );


        if (
                user.getRole()
                        !=
                        Role.ADMIN
        ) {

            throw new AccessDeniedException(
                    "Solo un administrador puede modificar la identidad de la empresa."
            );
        }
    }


    // =====================================================
    // FILE CLEANUP
    // =====================================================

    private void deleteAfterCommit(
            String fileName
    ) {

        if (
                TransactionSynchronizationManager
                        .isSynchronizationActive()
        ) {

            TransactionSynchronizationManager
                    .registerSynchronization(

                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    try {

                                        logoStorageService.delete(
                                                fileName
                                        );

                                    } catch (RuntimeException ignored) {
                                    }
                                }
                            }
                    );

            return;
        }


        logoStorageService.delete(
                fileName
        );
    }
}