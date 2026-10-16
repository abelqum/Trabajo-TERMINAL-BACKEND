package com.gestionmicroempresas.backend.service;

import com.gestionmicroempresas.backend.dto.request.CreateUserRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateUserRequest;

import com.gestionmicroempresas.backend.dto.response.UserAdministrationResponse;
import com.gestionmicroempresas.backend.dto.response.UserOptionResponse;
import com.gestionmicroempresas.backend.dto.response.UserPermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.UsersPagePermissionsResponse;
import com.gestionmicroempresas.backend.dto.response.UsersPageResponse;

import com.gestionmicroempresas.backend.entity.User;

import com.gestionmicroempresas.backend.enums.Role;

import com.gestionmicroempresas.backend.exception.ConflictException;
import com.gestionmicroempresas.backend.exception.ResourceNotFoundException;

import com.gestionmicroempresas.backend.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
    }


    // =====================================================
    // GET USERS
    // =====================================================

    @Transactional(readOnly = true)
    public UsersPageResponse getUsers(
            Long currentAdministratorId,
            int page,
            int size,
            String search,
            String role
    ) {

        User administrator =
                validateCurrentAdministrator(
                        currentAdministratorId
                );


        Long tenantId =
                administrator
                        .getTenant()
                        .getId();


        int safePage =
                Math.max(
                        page,
                        0
                );


        int safeSize =
                Math.min(

                        Math.max(
                                size,
                                1
                        ),

                        100
                );


        String normalizedSearch =
                normalizeSearch(
                        search
                );


        Role roleFilter =
                normalizeOptionalRole(
                        role
                );


        PageRequest pageable =
                PageRequest.of(

                        safePage,

                        safeSize,

                        Sort
                                .by(
                                        "name"
                                )
                                .ascending()
                );


        /*
         * Éste es el cambio fundamental:
         *
         * TODA consulta administrativa de usuarios
         * comienza limitada al tenant actual.
         */
        Specification<User> specification =
                (
                        root,
                        query,
                        criteriaBuilder
                ) ->

                        criteriaBuilder.equal(

                                root
                                        .get(
                                                "tenant"
                                        )
                                        .get(
                                                "id"
                                        ),

                                tenantId
                        );


        // =====================================================
        // SEARCH
        // =====================================================

        if (
                normalizedSearch != null
        ) {

            String searchPattern =
                    "%"
                            +
                            normalizedSearch
                                    .toLowerCase(
                                            Locale.ROOT
                                    )
                            +
                            "%";


            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.or(

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(
                                                            root.get(
                                                                    "name"
                                                            )
                                                    ),

                                                    searchPattern
                                            ),

                                            criteriaBuilder.like(

                                                    criteriaBuilder.lower(
                                                            root.get(
                                                                    "email"
                                                            )
                                                    ),

                                                    searchPattern
                                            )
                                    )
                    );
        }


        // =====================================================
        // ROLE FILTER
        // =====================================================

        if (
                roleFilter != null
        ) {

            specification =
                    specification.and(

                            (
                                    root,
                                    query,
                                    criteriaBuilder
                            ) ->

                                    criteriaBuilder.equal(

                                            root.get(
                                                    "role"
                                            ),

                                            roleFilter
                                    )
                    );
        }


        Page<User> result =
                userRepository.findAll(

                        specification,

                        pageable
                );


        long administratorCount =
                userRepository
                        .countByTenant_IdAndRole(

                                tenantId,

                                Role.ADMIN
                        );


        List<UserAdministrationResponse> users =
                result
                        .getContent()
                        .stream()
                        .map(
                                user ->

                                        toAdministrationResponse(

                                                user,

                                                currentAdministratorId,

                                                administratorCount
                                        )
                        )
                        .toList();


        return new UsersPageResponse(

                users,

                result.getNumber(),

                result.getSize(),

                result.getTotalElements(),

                result.getTotalPages(),

                result.isFirst(),

                result.isLast(),

                new UsersPagePermissionsResponse(
                        true
                )
        );
    }


    // =====================================================
    // CREATE USER
    // =====================================================

    @Transactional
    public UserAdministrationResponse createUser(
            Long currentAdministratorId,
            CreateUserRequest request
    ) {

        User administrator =
                validateCurrentAdministrator(
                        currentAdministratorId
                );


        String email =
                normalizeEmail(
                        request.email()
                );


        /*
         * Decisión actual:
         *
         * El email seguirá siendo globalmente único.
         * Esto permite iniciar sesión solamente con
         * email + password sin pedir una empresa.
         */
        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
        ) {

            throw new ConflictException(
                    "Ya existe un usuario registrado con ese correo."
            );
        }


        Role role =
                Role.fromApiValue(
                        request.role()
                );


        User user =
                new User(

                        administrator.getTenant(),

                        request
                                .name()
                                .trim(),

                        email,

                        passwordEncoder.encode(
                                request.password()
                        ),

                        role
                );


        User savedUser =
                userRepository.save(
                        user
                );


        long administratorCount =
                userRepository
                        .countByTenant_IdAndRole(

                                administrator
                                        .getTenant()
                                        .getId(),

                                Role.ADMIN
                        );


        return toAdministrationResponse(

                savedUser,

                currentAdministratorId,

                administratorCount
        );
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    @Transactional
    public UserAdministrationResponse updateUser(
            Long currentAdministratorId,
            Long userId,
            UpdateUserRequest request
    ) {

        User administrator =
                validateCurrentAdministrator(
                        currentAdministratorId
                );


        Long tenantId =
                administrator
                        .getTenant()
                        .getId();


        User user =
                getTenantUser(

                        tenantId,

                        userId
                );


        String newEmail =
                normalizeEmail(
                        request.email()
                );


        if (
                userRepository
                        .existsByEmailIgnoreCaseAndIdNot(

                                newEmail,

                                userId
                        )
        ) {

            throw new ConflictException(
                    "Ya existe otro usuario registrado con ese correo."
            );
        }


        Role newRole =
                Role.fromApiValue(
                        request.role()
                );


        if (
                userId.equals(
                        currentAdministratorId
                )
                        &&
                        user.getRole()
                                !=
                                newRole
        ) {

            throw new ConflictException(
                    "No puedes modificar tu propio rol."
            );
        }


        if (
                user.getRole()
                        ==
                        Role.ADMIN
                        &&
                        newRole
                                !=
                                Role.ADMIN
        ) {

            validateNotLastAdministrator(
                    tenantId
            );
        }


        user.setName(
                request
                        .name()
                        .trim()
        );


        user.setEmail(
                newEmail
        );


        user.setRole(
                newRole
        );


        User updatedUser =
                userRepository.save(
                        user
                );


        long administratorCount =
                userRepository
                        .countByTenant_IdAndRole(

                                tenantId,

                                Role.ADMIN
                        );


        return toAdministrationResponse(

                updatedUser,

                currentAdministratorId,

                administratorCount
        );
    }


    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @Transactional
    public void resetPassword(
            Long currentAdministratorId,
            Long userId,
            String newPassword
    ) {

        User administrator =
                validateCurrentAdministrator(
                        currentAdministratorId
                );


        if (
                userId.equals(
                        currentAdministratorId
                )
        ) {

            throw new ConflictException(
                    "Tu propia contraseña debe modificarse desde Configuración."
            );
        }


        User user =
                getTenantUser(

                        administrator
                                .getTenant()
                                .getId(),

                        userId
                );


        user.setPasswordHash(

                passwordEncoder.encode(
                        newPassword
                )
        );


        userRepository.save(
                user
        );
    }


    // =====================================================
    // DELETE USER
    // =====================================================

    @Transactional
    public void deleteUser(
            Long currentAdministratorId,
            Long userId
    ) {

        User administrator =
                validateCurrentAdministrator(
                        currentAdministratorId
                );


        Long tenantId =
                administrator
                        .getTenant()
                        .getId();


        if (
                userId.equals(
                        currentAdministratorId
                )
        ) {

            throw new ConflictException(
                    "No puedes eliminar tu propia cuenta."
            );
        }


        User user =
                getTenantUser(

                        tenantId,

                        userId
                );


        if (
                user.getRole()
                        ==
                        Role.ADMIN
        ) {

            validateNotLastAdministrator(
                    tenantId
            );
        }


        userRepository.delete(
                user
        );


        userRepository.flush();
    }


    // =====================================================
    // OPTIONS
    // =====================================================

    @Transactional(readOnly = true)
    public List<UserOptionResponse> getOptions(
            Long currentUserId
    ) {

        User currentUser =
                getCurrentUser(
                        currentUserId
                );


        return userRepository
                .findAllByTenant_IdOrderByNameAsc(

                        currentUser
                                .getTenant()
                                .getId()
                )
                .stream()
                .map(
                        UserOptionResponse::from
                )
                .toList();
    }


    // =====================================================
    // CURRENT USER
    // =====================================================

    private User getCurrentUser(
            Long userId
    ) {

        return userRepository
                .findById(
                        userId
                )
                .orElseThrow(
                        () ->
                                new AccessDeniedException(
                                        "La sesión no corresponde a un usuario válido."
                                )
                );
    }


    // =====================================================
    // TENANT USER
    // =====================================================

    private User getTenantUser(
            Long tenantId,
            Long userId
    ) {

        return userRepository
                .findByIdAndTenant_Id(

                        userId,

                        tenantId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "El usuario solicitado no existe."
                                )
                );
    }


    // =====================================================
    // CURRENT ADMIN
    // =====================================================

    private User validateCurrentAdministrator(
            Long userId
    ) {

        User administrator =
                getCurrentUser(
                        userId
                );


        if (
                administrator.getRole()
                        !=
                        Role.ADMIN
        ) {

            throw new AccessDeniedException(
                    "No tienes permisos para administrar usuarios."
            );
        }


        return administrator;
    }


    // =====================================================
    // LAST ADMIN
    // =====================================================

    private void validateNotLastAdministrator(
            Long tenantId
    ) {

        List<User> administrators =
                userRepository
                        .findAllByTenantAndRoleForUpdate(

                                tenantId,

                                Role.ADMIN
                        );


        if (
                administrators.size()
                        <=
                        1
        ) {

            throw new ConflictException(
                    "La operación no está permitida porque la empresa debe conservar al menos un administrador."
            );
        }
    }


    // =====================================================
    // RESPONSE
    // =====================================================

    private UserAdministrationResponse toAdministrationResponse(
            User user,
            Long currentAdministratorId,
            long administratorCount
    ) {

        boolean ownAccount =
                user
                        .getId()
                        .equals(
                                currentAdministratorId
                        );


        boolean lastAdministrator =
                user.getRole()
                        ==
                        Role.ADMIN

                        &&

                        administratorCount <= 1;


        UserPermissionsResponse permissions =
                new UserPermissionsResponse(

                        true,

                        !ownAccount
                                &&
                                !lastAdministrator,

                        !ownAccount
                                &&
                                !lastAdministrator,

                        !ownAccount
                );


        return UserAdministrationResponse.from(

                user,

                permissions
        );
    }


    // =====================================================
    // NORMALIZATION
    // =====================================================

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private String normalizeSearch(
            String search
    ) {

        if (
                search == null
                        ||
                        search.isBlank()
        ) {

            return null;
        }


        return search.trim();
    }


    private Role normalizeOptionalRole(
            String role
    ) {

        if (
                role == null
                        ||
                        role.isBlank()
        ) {

            return null;
        }


        return Role.fromApiValue(
                role
        );
    }
}