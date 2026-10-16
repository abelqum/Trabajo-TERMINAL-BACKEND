package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.IncomeRecordType;
import com.gestionmicroempresas.backend.enums.IncomeStatus;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "incomes")
public class Income
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
            name = "client_id",
            nullable = false
    )
    private Client client;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "record_type",
            nullable = false,
            length = 20
    )
    private IncomeRecordType recordType;


    @Column(
            name = "concept",
            nullable = false,
            length = 300
    )
    private String concept;


    @Column(
            name = "subtotal",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal subtotal;


    @Column(
            name = "tax_applied",
            nullable = false
    )
    private boolean taxApplied;


    @Column(
            name = "tax_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal taxAmount;


    @Column(
            name = "total",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal total;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private IncomeStatus status;


    @Column(
            name = "record_date",
            nullable = false
    )
    private LocalDate recordDate;


    @Column(
            name = "payment_date"
    )
    private LocalDate paymentDate;


    @Column(
            name = "payment_method",
            length = 80
    )
    private String paymentMethod;


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


    protected Income() {
    }


    public Income(
            Client client,
            IncomeRecordType recordType,
            String concept,
            BigDecimal subtotal,
            boolean taxApplied,
            BigDecimal taxAmount,
            BigDecimal total,
            IncomeStatus status,
            LocalDate recordDate,
            LocalDate paymentDate,
            String paymentMethod
    ) {

        super(
                client.getTenant()
        );

        this.client = client;
        this.recordType = recordType;
        this.concept = concept;
        this.subtotal = subtotal;
        this.taxApplied = taxApplied;
        this.taxAmount = taxAmount;
        this.total = total;
        this.status = status;
        this.recordDate = recordDate;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
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


        if (paymentMethod != null) {

            paymentMethod =
                    paymentMethod.trim();

            if (paymentMethod.isBlank()) {
                paymentMethod = null;
            }
        }
    }


    public Long getId() {
        return id;
    }


    public Client getClient() {
        return client;
    }


    public void setClient(
            Client client
    ) {
        this.client = client;
    }


    public IncomeRecordType getRecordType() {
        return recordType;
    }


    public void setRecordType(
            IncomeRecordType recordType
    ) {
        this.recordType = recordType;
    }


    public String getConcept() {
        return concept;
    }


    public void setConcept(
            String concept
    ) {
        this.concept = concept;
    }


    public BigDecimal getSubtotal() {
        return subtotal;
    }


    public void setSubtotal(
            BigDecimal subtotal
    ) {
        this.subtotal = subtotal;
    }


    public boolean isTaxApplied() {
        return taxApplied;
    }


    public void setTaxApplied(
            boolean taxApplied
    ) {
        this.taxApplied = taxApplied;
    }


    public BigDecimal getTaxAmount() {
        return taxAmount;
    }


    public void setTaxAmount(
            BigDecimal taxAmount
    ) {
        this.taxAmount = taxAmount;
    }


    public BigDecimal getTotal() {
        return total;
    }


    public void setTotal(
            BigDecimal total
    ) {
        this.total = total;
    }


    public IncomeStatus getStatus() {
        return status;
    }


    public void setStatus(
            IncomeStatus status
    ) {
        this.status = status;
    }


    public LocalDate getRecordDate() {
        return recordDate;
    }


    public void setRecordDate(
            LocalDate recordDate
    ) {
        this.recordDate = recordDate;
    }


    public LocalDate getPaymentDate() {
        return paymentDate;
    }


    public void setPaymentDate(
            LocalDate paymentDate
    ) {
        this.paymentDate = paymentDate;
    }


    public String getPaymentMethod() {
        return paymentMethod;
    }


    public void setPaymentMethod(
            String paymentMethod
    ) {
        this.paymentMethod = paymentMethod;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}