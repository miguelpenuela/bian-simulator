package com.example.biansimulator.catalog.application;

import com.example.biansimulator.catalog.domain.ElementType;
import java.util.Set;
import java.util.UUID;

/** Criterios de búsqueda; {@code versionId} nulo = versión activa más reciente. */
public record CatalogSearchCriteria(UUID versionId, Set<ElementType> types, String category, String text) {

    public CatalogSearchCriteria {
        types = types == null ? Set.of() : Set.copyOf(types);
    }
}
