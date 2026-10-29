package com.example.biansimulator.catalog.infrastructure;

import com.example.biansimulator.catalog.domain.CatalogVersion;
import com.example.biansimulator.catalog.domain.CatalogVersionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogVersionRepository extends JpaRepository<CatalogVersion, UUID> {

    List<CatalogVersion> findAllByOrderByImportedAtDesc();

    Optional<CatalogVersion> findFirstByStatusOrderByImportedAtDesc(CatalogVersionStatus status);
}
