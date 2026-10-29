package com.example.biansimulator.catalog.infrastructure;

import com.example.biansimulator.catalog.domain.CatalogRelationship;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogRelationshipRepository extends JpaRepository<CatalogRelationship, UUID> {

    @Query("""
            select r from CatalogRelationship r
            join fetch r.source join fetch r.target
            where r.source.id = :elementId or r.target.id = :elementId""")
    List<CatalogRelationship> findAllInvolving(@Param("elementId") UUID elementId);
}
