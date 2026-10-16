package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.TravelStatus;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "travels")
public class Travel
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;


    @Column(
            name = "destination",
            nullable = false,
            length = 150
    )
    private String destination;


    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;


    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private TravelStatus status;


    @Column(
            name = "budget_food",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal foodBudget;


    @Column(
            name = "budget_gasoline",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal gasolineBudget;


    @Column(
            name = "budget_tolls",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal tollsBudget;


    @Column(
            name = "budget_lodging",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal lodgingBudget;


    @Column(
            name = "budget_material",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal materialBudget;


    @Column(
            name = "budget_other",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal otherBudget;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "travel_users",
            joinColumns =
            @JoinColumn(
                    name = "travel_id"
            ),
            inverseJoinColumns =
            @JoinColumn(
                    name = "user_id"
            )
    )
    private Set<User> assignedUsers =
            new LinkedHashSet<>();


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


    protected Travel() {
    }


    public Travel(
            Tenant tenant,
            String name,
            String destination,
            LocalDate startDate,
            LocalDate endDate
    ) {

        super(
                tenant
        );

        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;

        this.status =
                TravelStatus.ACTIVE;

        this.foodBudget =
                BigDecimal.ZERO;

        this.gasolineBudget =
                BigDecimal.ZERO;

        this.tollsBudget =
                BigDecimal.ZERO;

        this.lodgingBudget =
                BigDecimal.ZERO;

        this.materialBudget =
                BigDecimal.ZERO;

        this.otherBudget =
                BigDecimal.ZERO;
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

        if (destination != null) {
            destination = destination.trim();
        }
    }


    public void replaceAssignedUsers(
            Collection<User> users
    ) {

        assignedUsers.clear();

        if (users != null) {

            assignedUsers.addAll(
                    users
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
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(
            String destination
    ) {
        this.destination = destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(
            LocalDate startDate
    ) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(
            LocalDate endDate
    ) {
        this.endDate = endDate;
    }

    public TravelStatus getStatus() {
        return status;
    }

    public void setStatus(
            TravelStatus status
    ) {
        this.status = status;
    }

    public BigDecimal getFoodBudget() {
        return foodBudget;
    }

    public void setFoodBudget(
            BigDecimal foodBudget
    ) {
        this.foodBudget = foodBudget;
    }

    public BigDecimal getGasolineBudget() {
        return gasolineBudget;
    }

    public void setGasolineBudget(
            BigDecimal gasolineBudget
    ) {
        this.gasolineBudget = gasolineBudget;
    }

    public BigDecimal getTollsBudget() {
        return tollsBudget;
    }

    public void setTollsBudget(
            BigDecimal tollsBudget
    ) {
        this.tollsBudget = tollsBudget;
    }

    public BigDecimal getLodgingBudget() {
        return lodgingBudget;
    }

    public void setLodgingBudget(
            BigDecimal lodgingBudget
    ) {
        this.lodgingBudget = lodgingBudget;
    }

    public BigDecimal getMaterialBudget() {
        return materialBudget;
    }

    public void setMaterialBudget(
            BigDecimal materialBudget
    ) {
        this.materialBudget = materialBudget;
    }

    public BigDecimal getOtherBudget() {
        return otherBudget;
    }

    public void setOtherBudget(
            BigDecimal otherBudget
    ) {
        this.otherBudget = otherBudget;
    }

    public Set<User> getAssignedUsers() {
        return assignedUsers;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}