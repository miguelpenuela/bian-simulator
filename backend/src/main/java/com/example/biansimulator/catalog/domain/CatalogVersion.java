package com.example.biansimulator.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Versión inmutable de un catálogo importado o de demostración (ADR-0004). */
@Entity
@Table(name = "catalog_version")
public class CatalogVersion {

    @Id
    private UUID id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private CatalogSourceType sourceType;

    @Column(name = "provenance_note", nullable = false)
    private String provenanceNote;

    @Column(name = "standard_release_label", length = 40)
    private String standardReleaseLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CatalogVersionStatus status;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    @Column(name = "imported_by")
    private UUID importedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CatalogVersion() {
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public CatalogSourceType getSourceType() {
        return sourceType;
    }

    public String getProvenanceNote() {
        return provenanceNote;
    }

    public String getStandardReleaseLabel() {
        return standardReleaseLabel;
    }

    public CatalogVersionStatus getStatus() {
        return status;
    }

    public Instant getImportedAt() {
        return importedAt;
    }

    /** Un catálogo es oficial solo si proviene de una importación oficial. */
    public boolean isOfficial() {
        return sourceType == CatalogSourceType.OFFICIAL_IMPORT;
    }
}
