package com.example.biansimulator.catalog.application;

import com.example.biansimulator.catalog.domain.CatalogElement;
import com.example.biansimulator.catalog.domain.CatalogSourceType;
import com.example.biansimulator.catalog.domain.CatalogVersion;
import com.example.biansimulator.catalog.domain.CatalogVersionStatus;
import com.example.biansimulator.catalog.domain.ElementType;
import com.example.biansimulator.catalog.domain.RelationshipProvenance;
import com.example.biansimulator.catalog.domain.RelationshipType;
import com.example.biansimulator.catalog.domain.ValidationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Vistas de lectura del catálogo (contratos de salida de la API). Nunca exponen entidades JPA. */
public final class CatalogViews {

    private CatalogViews() {
    }

    public record CatalogVersionResponse(UUID id, String code, String name, CatalogSourceType sourceType,
            boolean official, String provenanceNote, String standardReleaseLabel, CatalogVersionStatus status,
            Instant importedAt) {

        public static CatalogVersionResponse from(CatalogVersion v) {
            return new CatalogVersionResponse(v.getId(), v.getCode(), v.getName(), v.getSourceType(),
                    v.isOfficial(), v.getProvenanceNote(), v.getStandardReleaseLabel(), v.getStatus(),
                    v.getImportedAt());
        }
    }

    public record ElementSummary(UUID id, String externalRef, ElementType type, String name, String category,
            String description, ValidationStatus validationStatus) {

        public static ElementSummary from(CatalogElement e) {
            return new ElementSummary(e.getId(), e.getExternalRef(), e.getType(), e.getName(), e.getCategory(),
                    e.getDescription(), e.getValidationStatus());
        }
    }

    public enum Direction { OUTGOING, INCOMING }

    public record RelatedElement(UUID relationshipId, RelationshipType relationshipType, Direction direction,
            RelationshipProvenance provenance, String description, ElementSummary element) {
    }

    public record ElementDetailResponse(UUID id, String externalRef, ElementType type, String name,
            String description, String functionalResponsibility, String category,
            ValidationStatus validationStatus, String sourceReference, CatalogVersionResponse version,
            List<RelatedElement> relationships) {
    }

    public record FacetValue(String value, long count) {
    }

    public record CatalogFacetsResponse(UUID versionId, List<FacetValue> types, List<FacetValue> categories) {
    }
}
