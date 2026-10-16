package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CatalogItemRequest;
import com.gestionmicroempresas.backend.dto.request.ChangePasswordRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateProfileRequest;

import com.gestionmicroempresas.backend.dto.response.CatalogItemResponse;
import com.gestionmicroempresas.backend.dto.response.ConfigurationPermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.ConfigurationProfileResponse;
import com.gestionmicroempresas.backend.dto.response.ConfigurationResponse;

import com.gestionmicroempresas.backend.entity.InventoryCatalogItem;
import com.gestionmicroempresas.backend.entity.Tenant;
import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.InventoryCatalogType;
import com.gestionmicroempresas.backend.enums.Role;

import com.gestionmicroempresas.backend.exception.ConflictException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.InventoryCatalogRepository;
import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import java.util.List;

@Service
public class ConfigurationService {

    private final UserRepository userRepository;

    private final InventoryCatalogRepository catalogRepository;

    private final PasswordEncoder passwordEncoder;

    private final CurrentTenantService currentTenantService;


    public ConfigurationService(
            UserRepository userRepository,
            InventoryCatalogRepository catalogRepository,
            PasswordEncoder passwordEncoder,
            CurrentTenantService currentTenantService
    ) {

        this.userRepository = userRepository;
        this.catalogRepository = catalogRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentTenantService = currentTenantService;
    }


    // =====================================================
    // CONFIGURATION
    // =====================================================

    @Transactional(readOnly = true)
    public ConfigurationResponse getConfiguration(
            Long currentUserId
    ) {

        User user =
                getCurrentUser(
                        currentUserId
                );


        boolean administrator =
                user.getRole()
                        ==
                        Role.ADMIN;


        return new ConfigurationResponse(

                ConfigurationProfileResponse.from(
                        user
                ),

                new ConfigurationPermissionsResponse(
                        administrator,
                        administrator,
                        administrator
                )
        );
    }


    // =====================================================
    // PROFILE
    // =====================================================

    @Transactional
    public ConfigurationProfileResponse updateProfile(
            Long currentUserId,
            UpdateProfileRequest request
    ) {

        User user =
                getCurrentUser(
                        currentUserId
                );


        user.setName(
                request
                        .name()
                        .trim()
        );


        return ConfigurationProfileResponse.from(

                userRepository.saveAndFlush(
                        user
                )
        );
    }


    // =====================================================
    // PASSWORD
    // =====================================================

    @Transactional
    public void changePassword(
            Long currentUserId,
            ChangePasswordRequest request
    ) {

        User user =
                getCurrentUser(
                        currentUserId
                );


        if (
                !passwordEncoder.matches(
                        request.currentPassword(),
                        user.getPasswordHash()
                )
        ) {

            throw new IllegalArgumentException(
                    "La contraseña actual no es correcta."
            );
        }


        if (
                passwordEncoder.matches(
                        request.newPassword(),
                        user.getPasswordHash()
                )
        ) {

            throw new ConflictException(
                    "La nueva contraseña debe ser diferente a la contraseña actual."
            );
        }


        user.setPasswordHash(

                passwordEncoder.encode(
                        request.newPassword()
                )
        );


        userRepository.saveAndFlush(
                user
        );
    }


    // =====================================================
    // GET CATALOG
    // =====================================================

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> getCatalog(
            String catalogValue
    ) {

        InventoryCatalogType type =
                InventoryCatalogType.fromApiValue(
                        catalogValue
                );


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return catalogRepository
                .findAllByTenant_IdAndTypeOrderByNameAsc(
                        tenantId,
                        type
                )
                .stream()
                .map(
                        CatalogItemResponse::from
                )
                .toList();
    }


    // =====================================================
    // CREATE CATALOG ITEM
    // =====================================================

    @Transactional
    public CatalogItemResponse createCatalogItem(
            String catalogValue,
            CatalogItemRequest request
    ) {

        Tenant tenant =
                currentTenantService
                        .getCurrentTenant();


        InventoryCatalogType type =
                InventoryCatalogType.fromApiValue(
                        catalogValue
                );


        String name =
                request
                        .name()
                        .trim();


        if (
                catalogRepository
                        .existsByTenant_IdAndTypeAndNameIgnoreCase(
                                tenant.getId(),
                                type,
                                name
                        )
        ) {

            throw new ConflictException(
                    "Ya existe un registro con ese nombre en el catálogo."
            );
        }


        CatalogValues values =
                normalizeCatalogValues(
                        type,
                        request
                );


        InventoryCatalogItem item =
                new InventoryCatalogItem(
                        tenant,
                        type,
                        name,
                        values.link(),
                        values.allowPriceUpdate()
                );


        return CatalogItemResponse.from(

                catalogRepository.saveAndFlush(
                        item
                )
        );
    }


    // =====================================================
    // UPDATE CATALOG ITEM
    // =====================================================

    @Transactional
    public CatalogItemResponse updateCatalogItem(
            String catalogValue,
            Long itemId,
            CatalogItemRequest request
    ) {

        InventoryCatalogType type =
                InventoryCatalogType.fromApiValue(
                        catalogValue
                );


        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        InventoryCatalogItem item =
                getCatalogItem(
                        tenantId,
                        itemId,
                        type
                );


        String name =
                request
                        .name()
                        .trim();


        if (
                catalogRepository
                        .existsByTenant_IdAndTypeAndNameIgnoreCaseAndIdNot(
                                tenantId,
                                type,
                                name,
                                itemId
                        )
        ) {

            throw new ConflictException(
                    "Ya existe un registro con ese nombre en el catálogo."
            );
        }


        CatalogValues values =
                normalizeCatalogValues(
                        type,
                        request
                );


        item.setName(
                name
        );

        item.setLink(
                values.link()
        );

        item.setAllowPriceUpdate(
                values.allowPriceUpdate()
        );


        return CatalogItemResponse.from(

                catalogRepository.saveAndFlush(
                        item
                )
        );
    }


    // =====================================================
    // DELETE CATALOG ITEM
    // =====================================================

    @Transactional
    public void deleteCatalogItem(
            String catalogValue,
            Long itemId
    ) {

        InventoryCatalogType type =
                InventoryCatalogType.fromApiValue(
                        catalogValue
                );


        InventoryCatalogItem item =
                getCatalogItem(
                        currentTenantService
                                .getCurrentTenantId(),

                        itemId,

                        type
                );


        catalogRepository.delete(
                item
        );


        catalogRepository.flush();
    }


    // =====================================================
    // CATALOG VALUES
    // =====================================================

    private CatalogValues normalizeCatalogValues(
            InventoryCatalogType type,
            CatalogItemRequest request
    ) {

        String link = null;

        boolean allowPriceUpdate =
                false;


        if (type.supportsLink()) {

            link =
                    normalizeLink(
                            request.link()
                    );
        }


        if (type.supportsPriceUpdate()) {

            allowPriceUpdate =
                    request.allowPriceUpdate();
        }


        return new CatalogValues(
                link,
                allowPriceUpdate
        );
    }


    private String normalizeLink(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return null;
        }


        String normalized =
                value.trim();


        try {

            URI uri =
                    URI.create(
                            normalized
                    );


            String scheme =
                    uri.getScheme();


            if (
                    scheme == null
                            ||
                            (
                                    !"http".equalsIgnoreCase(
                                            scheme
                                    )
                                            &&
                                            !"https".equalsIgnoreCase(
                                                    scheme
                                            )
                            )
                            ||
                            uri.getHost() == null
            ) {

                throw new IllegalArgumentException(
                        "El enlace del proveedor no es válido."
                );
            }


            return normalized;

        } catch (
                IllegalArgumentException exception
        ) {

            throw new IllegalArgumentException(
                    "El enlace del proveedor no es válido."
            );
        }
    }


    // =====================================================
    // ENTITIES
    // =====================================================

    private InventoryCatalogItem getCatalogItem(
            Long tenantId,
            Long itemId,
            InventoryCatalogType type
    ) {

        return catalogRepository
                .findByIdAndTenant_IdAndType(
                        itemId,
                        tenantId,
                        type
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El registro del catálogo no existe."
                                )
                );
    }


    private User getCurrentUser(
            Long userId
    ) {

        Long tenantId =
                currentTenantService
                        .getCurrentTenantId();


        return userRepository
                .findByIdAndTenant_Id(
                        userId,
                        tenantId
                )
                .orElseThrow(
                        () ->
                                new AccessDeniedException(
                                        "La sesión no corresponde a un usuario válido."
                                )
                );
    }


    private record CatalogValues(
            String link,
            boolean allowPriceUpdate
    ) {
    }
}