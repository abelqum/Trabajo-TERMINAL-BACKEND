package com.gestionmicroempresas.backend.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "inventory_kit_components",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uq_inventory_kit_components",
                columnNames = {
                        "kit_product_id",
                        "component_product_id"
                }
        )
)
public class InventoryKitComponent
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "kit_product_id",
            nullable = false
    )
    private InventoryProduct kit;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "component_product_id",
            nullable = false
    )
    private InventoryProduct component;


    @Column(
            name = "required_quantity",
            nullable = false
    )
    private int requiredQuantity;


    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;


    protected InventoryKitComponent() {
    }


    public InventoryKitComponent(
            InventoryProduct kit,
            InventoryProduct component,
            int requiredQuantity
    ) {

        super(
                kit.getTenant()
        );

        this.kit = kit;
        this.component = component;
        this.requiredQuantity = requiredQuantity;
    }


    @PrePersist
    protected void beforeCreate() {

        createdAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }


    public Long getId() {
        return id;
    }


    public InventoryProduct getKit() {
        return kit;
    }


    public InventoryProduct getComponent() {
        return component;
    }


    public int getRequiredQuantity() {
        return requiredQuantity;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}