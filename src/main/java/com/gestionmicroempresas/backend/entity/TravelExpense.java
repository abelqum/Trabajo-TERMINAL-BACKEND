package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.TravelExpenseCategory;
import com.gestionmicroempresas.backend.enums.TravelExpenseStatus;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "travel_expenses")
public class TravelExpense
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
            name = "travel_id",
            nullable = false
    )
    private Travel travel;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 30
    )
    private TravelExpenseCategory category;


    @Column(
            name = "description",
            nullable = false,
            length = 300
    )
    private String description;


    @Column(
            name = "amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal amount;


    @Column(
            name = "expense_date",
            nullable = false
    )
    private LocalDate expenseDate;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private TravelExpenseStatus status;


    @Column(
            name = "rejection_reason",
            length = 500
    )
    private String rejectionReason;


    @Column(
            name = "receipt_file_name",
            length = 255
    )
    private String receiptFileName;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "created_by_user_id",
            nullable = false
    )
    private User createdBy;


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


    protected TravelExpense() {
    }


    public TravelExpense(
            Travel travel,
            TravelExpenseCategory category,
            String description,
            BigDecimal amount,
            LocalDate expenseDate,
            User createdBy
    ) {

        super(
                travel.getTenant()
        );

        this.travel = travel;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.createdBy = createdBy;

        this.status =
                TravelExpenseStatus.PENDING;
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

        if (rejectionReason != null) {

            rejectionReason =
                    rejectionReason.trim();

            if (rejectionReason.isBlank()) {
                rejectionReason = null;
            }
        }
    }


    public Long getId() {
        return id;
    }

    public Travel getTravel() {
        return travel;
    }

    public TravelExpenseCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public TravelExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(
            TravelExpenseStatus status
    ) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(
            String rejectionReason
    ) {
        this.rejectionReason =
                rejectionReason;
    }

    public String getReceiptFileName() {
        return receiptFileName;
    }

    public void setReceiptFileName(
            String receiptFileName
    ) {
        this.receiptFileName =
                receiptFileName;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}