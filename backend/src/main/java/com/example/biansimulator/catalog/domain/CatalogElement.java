package com.example.biansimulator.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "catalog_element")
public class CatalogElement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_version_id", nullable = false)
    private CatalogVersion version;

    @Column(name = "external_ref", nullable = false, length = 100)
    private String externalRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", nullable = false, length = 30)
    private ElementType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column
    private String description;

    @Column(name = "functional_responsibility")
    private String functionalResponsibility;

    @Column(length = 120)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 12)
    private ValidationStatus validationStatus;

    @Column(name = "source_reference", length = 500)
    private String sourceReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CatalogElement() {
    }

    public UUID getId() {
        return id;
    }

    public CatalogVersion getVersion() {
        return version;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public ElementType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFunctionalResponsibility() {
        return functionalResponsibility;
    }

    public String getCategory() {
        return category;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public String getSourceReference() {
        return sourceReference;
    }
}
