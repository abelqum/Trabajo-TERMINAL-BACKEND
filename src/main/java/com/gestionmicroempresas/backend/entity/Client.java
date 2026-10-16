package com.gestionmicroempresas.backend.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "clients")
public class Client
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "business_name",
            nullable = false,
            length = 200
    )
    private String businessName;


    @Column(
            name = "rfc",
            length = 13
    )
    private String rfc;


    @ElementCollection
    @CollectionTable(
            name = "client_phones",
            joinColumns = @JoinColumn(
                    name = "client_id"
            )
    )
    @Column(
            name = "phone",
            nullable = false,
            length = 30
    )
    private Set<String> phones =
            new LinkedHashSet<>();


    @ElementCollection
    @CollectionTable(
            name = "client_emails",
            joinColumns = @JoinColumn(
                    name = "client_id"
            )
    )
    @Column(
            name = "email",
            nullable = false,
            length = 200
    )
    private Set<String> emails =
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


    protected Client() {
    }


    public Client(
            Tenant tenant,
            String businessName,
            String rfc
    ) {

        super(
                tenant
        );

        this.businessName =
                businessName;

        this.rfc =
                rfc;
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

        if (businessName != null) {

            businessName =
                    businessName.trim();
        }

        if (rfc != null) {

            rfc =
                    rfc
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );

            if (rfc.isBlank()) {
                rfc = null;
            }
        }
    }


    public void replacePhones(
            Set<String> phones
    ) {

        this.phones.clear();

        if (phones != null) {
            this.phones.addAll(
                    phones
            );
        }
    }


    public void replaceEmails(
            Set<String> emails
    ) {

        this.emails.clear();

        if (emails != null) {
            this.emails.addAll(
                    emails
            );
        }
    }


    public Long getId() {
        return id;
    }


    public String getBusinessName() {
        return businessName;
    }


    public void setBusinessName(
            String businessName
    ) {
        this.businessName =
                businessName;
    }


    public String getRfc() {
        return rfc;
    }


    public void setRfc(
            String rfc
    ) {
        this.rfc = rfc;
    }


    public Set<String> getPhones() {
        return phones;
    }


    public Set<String> getEmails() {
        return emails;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}