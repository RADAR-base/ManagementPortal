package org.radarcns.management.repository;

import java.util.Optional;
import org.radarcns.management.domain.SourceData;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the SourceData entity.
 */
@SuppressWarnings("unused")
public interface SourceDataRepository extends JpaRepository<SourceData, Long> {

    Optional<SourceData> findOneBySourceDataName(String sourceDataName);
}
