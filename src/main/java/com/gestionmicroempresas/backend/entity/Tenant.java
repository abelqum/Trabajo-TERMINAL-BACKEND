package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.TenantStatus;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.Locale;

@Entity
@Table(name = "tenants")
public class Tenant {

    public static final String DEFAULT_PRIMARY_COLOR =
            "#1D4ED8";


    public static final String DEFAULT_SIDEBAR_COLOR =
            "#172554";


    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "name",
            nullable = false,
            length = 200
    )
    private String name;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private TenantStatus status;


    @Column(
            name = "logo_file_name",
            length = 255
    )
    private String logoFileName;


    @Column(
            name = "primary_color",
            nullable = false,
            length = 7
    )
    private String primaryColor;


    @Column(
            name = "sidebar_color",
            nullable = false,
            length = 7
    )
    private String sidebarColor;


    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;


    protected Tenant() {
    }


    public Tenant(
            String name
    ) {

        this.name =
                name;

        this.status =
                TenantStatus.ACTIVE;

        this.primaryColor =
                DEFAULT_PRIMARY_COLOR;

        this.sidebarColor =
                DEFAULT_SIDEBAR_COLOR;
    }


    @PrePersist
    protected void beforeCreate() {

        normalize();

        OffsetDateTime now =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );


        if (status == null) {

            status =
                    TenantStatus.ACTIVE;
        }


        if (primaryColor == null) {

            primaryColor =
                    DEFAULT_PRIMARY_COLOR;
        }


        if (sidebarColor == null) {

            sidebarColor =
                    DEFAULT_SIDEBAR_COLOR;
        }


        createdAt =
                now;

        updatedAt =
                now;
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

            name =
                    name.trim();
        }


        if (logoFileName != null) {

            logoFileName =
                    logoFileName.trim();

            if (logoFileName.isBlank()) {

                logoFileName =
                        null;
            }
        }


        if (primaryColor != null) {

            primaryColor =
                    primaryColor
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );
        }


        if (sidebarColor != null) {

            sidebarColor =
                    sidebarColor
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );
        }
    }


    public Long getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public void setName(
            String name
    ) {

        this.name =
                name;
    }


    public TenantStatus getStatus() {
        return status;
    }


    public void setStatus(
            TenantStatus status
    ) {

        this.status =
                status;
    }


    public String getLogoFileName() {
        return logoFileName;
    }


    public void setLogoFileName(
            String logoFileName
    ) {

        this.logoFileName =
                logoFileName;
    }


    public String getPrimaryColor() {
        return primaryColor;
    }


    public void setPrimaryColor(
            String primaryColor
    ) {

        this.primaryColor =
                primaryColor;
    }


    public String getSidebarColor() {
        return sidebarColor;
    }


    public void setSidebarColor(
            String sidebarColor
    ) {

        this.sidebarColor =
                sidebarColor;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}