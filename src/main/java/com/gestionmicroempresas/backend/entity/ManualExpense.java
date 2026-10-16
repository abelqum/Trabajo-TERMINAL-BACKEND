package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.ExpenseCategory;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "manual_expenses")
public class ManualExpense
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "concept",
            nullable = false,
            length = 150
    )
    private String concept;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 30
    )
    private ExpenseCategory category;


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


    @Column(
            name = "description",
            length = 500
    )
    private String description;


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


    protected ManualExpense() {
    }


    public ManualExpense(
            Tenant tenant,
            String concept,
            ExpenseCategory category,
            BigDecimal amount,
            LocalDate expenseDate,
            String description
    ) {

        super(
                tenant
        );

        this.concept = concept;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
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

        if (concept != null) {

            concept =
                    concept.trim();
        }


        if (description != null) {

            description =
                    description.trim();

            if (description.isBlank()) {
                description = null;
            }
        }
    }


    public Long getId() {
        return id;
    }


    public String getConcept() {
        return concept;
    }


    public void setConcept(
            String concept
    ) {
        this.concept = concept;
    }


    public ExpenseCategory getCategory() {
        return category;
    }


    public void setCategory(
            ExpenseCategory category
    ) {
        this.category = category;
    }


    public BigDecimal getAmount() {
        return amount;
    }


    public void setAmount(
            BigDecimal amount
    ) {
        this.amount = amount;
    }


    public LocalDate getExpenseDate() {
        return expenseDate;
    }


    public void setExpenseDate(
            LocalDate expenseDate
    ) {
        this.expenseDate = expenseDate;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(
            String description
    ) {
        this.description = description;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}