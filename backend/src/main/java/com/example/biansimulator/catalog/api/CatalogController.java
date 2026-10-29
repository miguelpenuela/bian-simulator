package com.example.biansimulator.catalog.api;

import com.example.biansimulator.catalog.application.CatalogViews.CatalogFacetsResponse;
import com.example.biansimulator.catalog.application.CatalogViews.CatalogVersionResponse;
import com.example.biansimulator.catalog.application.CatalogViews.ElementDetailResponse;
import com.example.biansimulator.catalog.application.CatalogViews.ElementSummary;
import com.example.biansimulator.catalog.application.CatalogQueryService;
import com.example.biansimulator.catalog.application.CatalogSearchCriteria;
import com.example.biansimulator.catalog.domain.ElementType;
import com.example.biansimulator.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
@PreAuthorize("hasAuthority('CATALOG_READ')")
@Validated
@Tag(name = "Catálogo BIAN")
public class CatalogController {

    private final CatalogQueryService catalog;

    public CatalogController(CatalogQueryService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/versions")
    @Operation(summary = "Versiones de catálogo disponibles, con su procedencia")
    public List<CatalogVersionResponse> versions() {
        return catalog.listVersions();
    }

    @GetMapping("/versions/{versionId}/facets")
    @Operation(summary = "Conteos por tipo de elemento y categoría, para filtros")
    public CatalogFacetsResponse facets(@PathVariable UUID versionId) {
        return catalog.facets(versionId);
    }

    @GetMapping("/elements")
    @Operation(summary = "Búsqueda paginada de elementos del catálogo")
    public PageResponse<ElementSummary> search(
            @RequestParam(required = false) UUID versionId,
            @RequestParam(name = "type", required = false) Set<ElementType> types,
            @RequestParam(required = false) @Size(max = 120) String category,
            @RequestParam(name = "q", required = false) @Size(max = 100) String text,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return catalog.search(new CatalogSearchCriteria(versionId, types, category, text), page, size);
    }

    @GetMapping("/elements/{id}")
    @Operation(summary = "Ficha de un elemento con sus relaciones")
    public ElementDetailResponse element(@PathVariable UUID id) {
        return catalog.getElement(id);
    }

    @GetMapping("/service-domains/{id}")
    @Operation(summary = "Ficha de un Service Domain (404 si el elemento no es un Service Domain)")
    public ElementDetailResponse serviceDomain(@PathVariable UUID id) {
        return catalog.getServiceDomain(id);
    }
}
