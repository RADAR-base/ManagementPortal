package org.radarbase.management.repository;


import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.ConnectDataLog;
import org.radarbase.management.domain.enumeration.DataGroupingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

@RepositoryDefinition(domainClass = ConnectDataLog.class, idClass = String.class)
public interface ConnectDataLogRepository extends JpaRepository<ConnectDataLog, String>,
        RevisionRepository<ConnectDataLog, String, Integer> {

    Optional<ConnectDataLog> findFirstByUserIdAndDataGroupingTypeOrderByTimeDesc(String userId, DataGroupingType dataGroupingType);
}
