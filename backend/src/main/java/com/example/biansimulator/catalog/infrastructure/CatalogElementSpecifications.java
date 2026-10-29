package com.example.biansimulator.catalog.infrastructure;

import com.example.biansimulator.catalog.domain.CatalogElement;
import com.example.biansimulator.catalog.domain.ElementType;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** Criterios de búsqueda del catálogo. Todos los valores se pasan como parámetros (sin SQL dinámico). */
public final class CatalogElementSpecifications {

    private CatalogElementSpecifications() {
    }

    public static Specification<CatalogElement> inVersion(UUID versionId) {
        return (root, query, cb) -> cb.equal(root.get("version").get("id"), versionId);
    }

    public static Specification<CatalogElement> ofTypes(Set<ElementType> types) {
        return (root, query, cb) -> types == null || types.isEmpty() ? null : root.get("type").in(types);
    }

    public static Specification<CatalogElement> inCategory(String category) {
        return (root, query, cb) -> category == null || category.isBlank() ? null
                : cb.equal(root.get("category"), category);
    }

    /** Coincidencia parcial, sin distinguir mayúsculas, en nombre o descripción. */
    public static Specification<CatalogElement> matchesText(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return null;
            }
            String pattern = "%" + escapeLike(text.trim().toLowerCase(Locale.ROOT)) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("description")), pattern, '\\'));
        };
    }

    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
