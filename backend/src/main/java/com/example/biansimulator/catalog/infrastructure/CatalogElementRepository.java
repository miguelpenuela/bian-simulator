package com.example.biansimulator.catalog.infrastructure;

import com.example.biansimulator.catalog.domain.CatalogElement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogElementRepository
        extends JpaRepository<CatalogElement, UUID>, JpaSpecificationExecutor<CatalogElement> {

    @EntityGraph(attributePaths = "version")
    @Query("select e from CatalogElement e where e.id = :id")
    Optional<CatalogElement> findWithVersionById(@Param("id") UUID id);

    @Query("""
            select e.type as facet, count(e) as total from CatalogElement e
            where e.version.id = :versionId group by e.type order by e.type""")
    List<FacetCount> countByType(@Param("versionId") UUID versionId);

    @Query("""
            select e.category as facet, count(e) as total from CatalogElement e
            where e.version.id = :versionId and e.category is not null
            group by e.category order by e.category""")
    List<FacetCount> countByCategory(@Param("versionId") UUID versionId);

    interface FacetCount {
        Object getFacet();

        long getTotal();
    }
}
