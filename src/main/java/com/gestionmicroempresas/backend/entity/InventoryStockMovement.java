package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.InventoryMovementType;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "inventory_stock_movements")
public class InventoryStockMovement
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
            name = "product_id",
            nullable = false
    )
    private InventoryProduct product;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "movement_type",
            nullable = false,
            length = 30
    )
    private InventoryMovementType movementType;


    @Column(
            name = "quantity_change",
            nullable = false
    )
    private int quantityChange;


    @Column(
            name = "resulting_quantity",
            nullable = false
    )
    private int resultingQuantity;


    @Column(
            name = "reason",
            length = 500
    )
    private String reason;


    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;


    protected InventoryStockMovement() {
    }


    public InventoryStockMovement(
            InventoryProduct product,
            User user,
            InventoryMovementType movementType,
            int quantityChange,
            int resultingQuantity,
            String reason
    ) {

        super(
                product.getTenant()
        );

        this.product = product;
        this.user = user;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.resultingQuantity = resultingQuantity;
        this.reason = reason;
    }


    @PrePersist
    protected void beforeCreate() {

        if (reason != null) {

            reason = reason.trim();

            if (reason.isBlank()) {
                reason = null;
            }
        }


        createdAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }


    public Long getId() {
        return id;
    }


    public InventoryProduct getProduct() {
        return product;
    }


    public User getUser() {
        return user;
    }


    public InventoryMovementType getMovementType() {
        return movementType;
    }


    public int getQuantityChange() {
        return quantityChange;
    }


    public int getResultingQuantity() {
        return resultingQuantity;
    }


    public String getReason() {
        return reason;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}