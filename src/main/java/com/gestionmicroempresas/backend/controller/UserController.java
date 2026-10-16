package com.gestionmicroempresas.backend.controller;

import com.gestionmicroempresas.backend.dto.request.CreateUserRequest;
import com.gestionmicroempresas.backend.dto.request.ResetUserPasswordRequest;
import com.gestionmicroempresas.backend.dto.request.UpdateUserRequest;

import com.gestionmicroempresas.backend.dto.response.UserAdministrationResponse;
import com.gestionmicroempresas.backend.dto.response.UserOptionResponse;
import com.gestionmicroempresas.backend.dto.response.UsersPageResponse;

import com.gestionmicroempresas.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;


    public UserController(
            UserService userService
    ) {

        this.userService =
                userService;
    }


    // =====================================================
    // GET USERS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersPageResponse> getUsers(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    required = false
            )
            String search,

            @RequestParam(
                    name = "rol",
                    required = false
            )
            String role
    ) {

        return ResponseEntity.ok(

                userService.getUsers(

                        getUserId(jwt),

                        page,

                        size,

                        search,

                        role
                )
        );
    }


    // =====================================================
    // CREATE USER
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdministrationResponse> createUser(

            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestBody
            CreateUserRequest request
    ) {

        UserAdministrationResponse user =
                userService.createUser(

                        getUserId(jwt),

                        request
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        user
                );
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdministrationResponse> updateUser(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateUserRequest request
    ) {

        return ResponseEntity.ok(

                userService.updateUser(

                        getUserId(jwt),

                        id,

                        request
                )
        );
    }


    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            ResetUserPasswordRequest request
    ) {

        userService.resetPassword(

                getUserId(jwt),

                id,

                request.password()
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // DELETE USER
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id
    ) {

        userService.deleteUser(

                getUserId(jwt),

                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // OPTIONS
    // =====================================================
    @GetMapping("/opciones")
    public ResponseEntity<List<UserOptionResponse>> getOptions(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestParam(
                    name = "asignablesViaje",
                    defaultValue = "false"
            )
            boolean assignableToTravel,

            @RequestParam(
                    name = "asignablesTarea",
                    defaultValue = "false"
            )
            boolean assignableToTask
    ) {

        return ResponseEntity.ok(

                userService.getOptions(
                        getUserId(jwt)
                )
        );
    }

    // =====================================================
    // JWT USER ID
    // =====================================================

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