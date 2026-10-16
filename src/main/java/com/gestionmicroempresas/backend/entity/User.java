package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.Role;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.Locale;

@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "tenant_id",
            nullable = false,
            updatable = false
    )
    private Tenant tenant;


    @Column(
            name = "nombre",
            nullable = false,
            length = 150
    )
    private String name;


    @Column(
            name = "email",
            nullable = false,
            length = 200
    )
    private String email;


    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;


    @Column(
            name = "rol",
            nullable = false,
            length = 20
    )
    private Role role;


    @Column(
            name = "creado_en",
            nullable = false
    )
    private OffsetDateTime createdAt;


    @Column(
            name = "actualizado_en",
            nullable = false
    )
    private OffsetDateTime updatedAt;


    protected User() {
    }


    public User(
            Tenant tenant,
            String name,
            String email,
            String passwordHash,
            Role role
    ) {

        this.tenant = tenant;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }


    @PrePersist
    protected void beforeCreate() {

        normalize();

        OffsetDateTime now =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    protected void beforeUpdate() {

        normalize();

        updatedAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }


    private void normalize() {

        if (name != null) {
            name = name.trim();
        }


        if (email != null) {

            email =
                    email
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );
        }
    }


    public Long getId() {
        return id;
    }


    public Tenant getTenant() {
        return tenant;
    }


    public Long getTenantId() {

        return tenant == null
                ?
                null
                :
                tenant.getId();
    }


    public String getName() {
        return name;
    }


    public void setName(
            String name
    ) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(
            String email
    ) {
        this.email = email;
    }


    public String getPasswordHash() {
        return passwordHash;
    }


    public void setPasswordHash(
            String passwordHash
    ) {
        this.passwordHash = passwordHash;
    }


    public Role getRole() {
        return role;
    }


    public void setRole(
            Role role
    ) {
        this.role = role;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}