package com.gestionmicroempresas.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "inventory_products")
public class InventoryProduct
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "description",
            nullable = false,
            length = 200
    )
    private String description;


    @Column(
            name = "model",
            length = 100
    )
    private String model;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private InventoryCatalogItem category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private InventoryCatalogItem brand;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_measure_id")
    private InventoryCatalogItem unitMeasure;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private InventoryCatalogItem supplier;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measure_id")
    private InventoryCatalogItem measure;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private InventoryCatalogItem warehouse;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id")
    private InventoryCatalogItem condition;


    @Column(
            name = "row_location",
            length = 50
    )
    private String rowLocation;


    @Column(
            name = "unit_price",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal unitPrice;


    @Column(
            name = "quantity",
            nullable = false
    )
    private int quantity;


    @Column(
            name = "minimum_stock",
            nullable = false
    )
    private int minimumStock;


    @Column(
            name = "is_kit",
            nullable = false
    )
    private boolean kit;


    @Column(
            name = "photo_file_name",
            length = 255
    )
    private String photoFileName;


    @Column(
            name = "qr_code",
            length = 100
    )
    private String qrCode;


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


    protected InventoryProduct() {
    }


    public InventoryProduct(
            Tenant tenant,
            String description,
            String model,
            InventoryCatalogItem category,
            InventoryCatalogItem brand,
            InventoryCatalogItem unitMeasure,
            InventoryCatalogItem supplier,
            InventoryCatalogItem measure,
            InventoryCatalogItem warehouse,
            InventoryCatalogItem condition,
            String rowLocation,
            BigDecimal unitPrice,
            int quantity,
            int minimumStock
    ) {

        super(
                tenant
        );

        this.description = description;
        this.model = model;
        this.category = category;
        this.brand = brand;
        this.unitMeasure = unitMeasure;
        this.supplier = supplier;
        this.measure = measure;
        this.warehouse = warehouse;
        this.condition = condition;
        this.rowLocation = rowLocation;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.minimumStock = minimumStock;

        this.kit = false;
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

        if (description != null) {
            description = description.trim();
        }


        if (model != null) {

            model = model.trim();

            if (model.isBlank()) {
                model = null;
            }
        }


        if (rowLocation != null) {

            rowLocation =
                    rowLocation.trim();

            if (rowLocation.isBlank()) {
                rowLocation = null;
            }
        }
    }


    public Long getId() {
        return id;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(
            String description
    ) {
        this.description = description;
    }


    public String getModel() {
        return model;
    }


    public void setModel(
            String model
    ) {
        this.model = model;
    }


    public InventoryCatalogItem getCategory() {
        return category;
    }


    public void setCategory(
            InventoryCatalogItem category
    ) {
        this.category = category;
    }


    public InventoryCatalogItem getBrand() {
        return brand;
    }


    public void setBrand(
            InventoryCatalogItem brand
    ) {
        this.brand = brand;
    }


    public InventoryCatalogItem getUnitMeasure() {
        return unitMeasure;
    }


    public void setUnitMeasure(
            InventoryCatalogItem unitMeasure
    ) {
        this.unitMeasure = unitMeasure;
    }


    public InventoryCatalogItem getSupplier() {
        return supplier;
    }


    public void setSupplier(
            InventoryCatalogItem supplier
    ) {
        this.supplier = supplier;
    }


    public InventoryCatalogItem getMeasure() {
        return measure;
    }


    public void setMeasure(
            InventoryCatalogItem measure
    ) {
        this.measure = measure;
    }


    public InventoryCatalogItem getWarehouse() {
        return warehouse;
    }


    public void setWarehouse(
            InventoryCatalogItem warehouse
    ) {
        this.warehouse = warehouse;
    }


    public InventoryCatalogItem getCondition() {
        return condition;
    }


    public void setCondition(
            InventoryCatalogItem condition
    ) {
        this.condition = condition;
    }


    public String getRowLocation() {
        return rowLocation;
    }


    public void setRowLocation(
            String rowLocation
    ) {
        this.rowLocation = rowLocation;
    }


    public BigDecimal getUnitPrice() {
        return unitPrice;
    }


    public void setUnitPrice(
            BigDecimal unitPrice
    ) {
        this.unitPrice = unitPrice;
    }


    public int getQuantity() {
        return quantity;
    }


    public void setQuantity(
            int quantity
    ) {
        this.quantity = quantity;
    }


    public int getMinimumStock() {
        return minimumStock;
    }


    public void setMinimumStock(
            int minimumStock
    ) {
        this.minimumStock = minimumStock;
    }


    public boolean isKit() {
        return kit;
    }


    public void setKit(
            boolean kit
    ) {
        this.kit = kit;
    }


    public String getPhotoFileName() {
        return photoFileName;
    }


    public void setPhotoFileName(
            String photoFileName
    ) {
        this.photoFileName = photoFileName;
    }


    public String getQrCode() {
        return qrCode;
    }


    public void setQrCode(
            String qrCode
    ) {
        this.qrCode = qrCode;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}