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
@Table(name = "catalog_relationship")
public class CatalogRelationship {

    @Id
    private UUID id;

    @Column(name = "catalog_version_id", nullable = false)
    private UUID versionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_element_id", nullable = false)
    private CatalogElement source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_element_id", nullable = false)
    private CatalogElement target;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 40)
    private RelationshipType type;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RelationshipProvenance provenance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CatalogRelationship() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public CatalogElement getSource() {
        return source;
    }

    public CatalogElement getTarget() {
        return target;
    }

    public RelationshipType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public RelationshipProvenance getProvenance() {
        return provenance;
    }
}
