package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.InventoryCatalogType;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "inventory_catalog_items")
public class InventoryCatalogItem
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "catalog_type",
            nullable = false,
            length = 30
    )
    private InventoryCatalogType type;


    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;


    @Column(
            name = "link",
            length = 500
    )
    private String link;


    @Column(
            name = "allow_price_update",
            nullable = false
    )
    private boolean allowPriceUpdate;


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


    protected InventoryCatalogItem() {
    }


    public InventoryCatalogItem(
            Tenant tenant,
            InventoryCatalogType type,
            String name,
            String link,
            boolean allowPriceUpdate
    ) {

        super(
                tenant
        );

        this.type = type;
        this.name = name;
        this.link = link;
        this.allowPriceUpdate =
                allowPriceUpdate;
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

            name =
                    name.trim();
        }


        if (link != null) {

            link =
                    link.trim();

            if (link.isBlank()) {
                link = null;
            }
        }
    }


    public Long getId() {
        return id;
    }


    public InventoryCatalogType getType() {
        return type;
    }


    public void setType(
            InventoryCatalogType type
    ) {
        this.type = type;
    }


    public String getName() {
        return name;
    }


    public void setName(
            String name
    ) {
        this.name = name;
    }


    public String getLink() {
        return link;
    }


    public void setLink(
            String link
    ) {
        this.link = link;
    }


    public boolean isAllowPriceUpdate() {
        return allowPriceUpdate;
    }


    public void setAllowPriceUpdate(
            boolean allowPriceUpdate
    ) {
        this.allowPriceUpdate =
                allowPriceUpdate;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}