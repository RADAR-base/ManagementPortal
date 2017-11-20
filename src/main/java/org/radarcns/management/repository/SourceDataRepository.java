package org.radarcns.management.repository;

import org.radarcns.management.domain.SourceData;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the SourceData entity.
 */
@SuppressWarnings("unused")
public interface SourceDataRepository extends JpaRepository<SourceData,Long> {
    Optional<SourceData> findOneBySourceDataType(String sourceDataType);
}
