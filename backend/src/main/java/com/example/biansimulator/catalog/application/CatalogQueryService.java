package com.example.biansimulator.catalog.application;

import static com.example.biansimulator.catalog.infrastructure.CatalogElementSpecifications.inCategory;
import static com.example.biansimulator.catalog.infrastructure.CatalogElementSpecifications.inVersion;
import static com.example.biansimulator.catalog.infrastructure.CatalogElementSpecifications.matchesText;
import static com.example.biansimulator.catalog.infrastructure.CatalogElementSpecifications.ofTypes;

import com.example.biansimulator.catalog.application.CatalogViews.CatalogFacetsResponse;
import com.example.biansimulator.catalog.application.CatalogViews.CatalogVersionResponse;
import com.example.biansimulator.catalog.application.CatalogViews.Direction;
import com.example.biansimulator.catalog.application.CatalogViews.ElementDetailResponse;
import com.example.biansimulator.catalog.application.CatalogViews.ElementSummary;
import com.example.biansimulator.catalog.application.CatalogViews.FacetValue;
import com.example.biansimulator.catalog.application.CatalogViews.RelatedElement;
import com.example.biansimulator.catalog.domain.CatalogElement;
import com.example.biansimulator.catalog.domain.CatalogRelationship;
import com.example.biansimulator.catalog.domain.CatalogVersion;
import com.example.biansimulator.catalog.domain.CatalogVersionStatus;
import com.example.biansimulator.catalog.domain.ElementType;
import com.example.biansimulator.catalog.infrastructure.CatalogElementRepository;
import com.example.biansimulator.catalog.infrastructure.CatalogElementRepository.FacetCount;
import com.example.biansimulator.catalog.infrastructure.CatalogRelationshipRepository;
import com.example.biansimulator.catalog.infrastructure.CatalogVersionRepository;
import com.example.biansimulator.shared.api.PageResponse;
import com.example.biansimulator.shared.error.ResourceNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de lectura del catálogo. */
@Service
@Transactional(readOnly = true)
public class CatalogQueryService {

    private final CatalogVersionRepository versions;
    private final CatalogElementRepository elements;
    private final CatalogRelationshipRepository relationships;

    public CatalogQueryService(CatalogVersionRepository versions, CatalogElementRepository elements,
            CatalogRelationshipRepository relationships) {
        this.versions = versions;
        this.elements = elements;
        this.relationships = relationships;
    }

    public List<CatalogVersionResponse> listVersions() {
        return versions.findAllByOrderByImportedAtDesc().stream().map(CatalogVersionResponse::from).toList();
    }

    public CatalogFacetsResponse facets(UUID versionId) {
        CatalogVersion version = versions.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión de catálogo", versionId));
        return new CatalogFacetsResponse(version.getId(),
                toFacets(elements.countByType(version.getId())),
                toFacets(elements.countByCategory(version.getId())));
    }

    public PageResponse<ElementSummary> search(CatalogSearchCriteria criteria, int page, int size) {
        UUID versionId = criteria.versionId() != null ? criteria.versionId() : defaultVersionId();
        var spec = inVersion(versionId)
                .and(ofTypes(criteria.types()))
                .and(inCategory(criteria.category()))
                .and(matchesText(criteria.text()));
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending().and(Sort.by("id")));
        return PageResponse.from(elements.findAll(spec, pageable), ElementSummary::from);
    }

    public ElementDetailResponse getElement(UUID id) {
        CatalogElement element = elements.findWithVersionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Elemento de catálogo", id));
        return toDetail(element);
    }

    public ElementDetailResponse getServiceDomain(UUID id) {
        CatalogElement element = elements.findWithVersionById(id)
                .filter(e -> e.getType() == ElementType.SERVICE_DOMAIN)
                .orElseThrow(() -> new ResourceNotFoundException("Service Domain", id));
        return toDetail(element);
    }

    private ElementDetailResponse toDetail(CatalogElement element) {
        List<RelatedElement> related = relationships.findAllInvolving(element.getId()).stream()
                .map(r -> toRelated(element.getId(), r))
                .sorted(Comparator.comparing((RelatedElement r) -> r.relationshipType())
                        .thenComparing(r -> r.element().name()))
                .toList();
        return new ElementDetailResponse(element.getId(), element.getExternalRef(), element.getType(),
                element.getName(), element.getDescription(), element.getFunctionalResponsibility(),
                element.getCategory(), element.getValidationStatus(), element.getSourceReference(),
                CatalogVersionResponse.from(element.getVersion()), related);
    }

    private static RelatedElement toRelated(UUID elementId, CatalogRelationship r) {
        boolean outgoing = r.getSource().getId().equals(elementId);
        CatalogElement other = outgoing ? r.getTarget() : r.getSource();
        return new RelatedElement(r.getId(), r.getType(), outgoing ? Direction.OUTGOING : Direction.INCOMING,
                r.getProvenance(), r.getDescription(), ElementSummary.from(other));
    }

    private UUID defaultVersionId() {
        return versions.findFirstByStatusOrderByImportedAtDesc(CatalogVersionStatus.ACTIVE)
                .map(CatalogVersion::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión de catálogo activa", "-"));
    }

    private static List<FacetValue> toFacets(List<FacetCount> counts) {
        return counts.stream().map(c -> new FacetValue(String.valueOf(c.getFacet()), c.getTotal())).toList();
    }
}
