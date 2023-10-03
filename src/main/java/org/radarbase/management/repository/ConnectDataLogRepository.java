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

    @Query(
            value = "SELECT * FROM connect_data_log u WHERE  \"userId\" is not null and \"userId\"=?1  and \"dataGroupingType\" is not null and \"dataGroupingType\"=?2 ORDER BY u.time DESC",
            nativeQuery = true)
    Optional<ConnectDataLog> findDataLogsByUserIdAndDataGroupingType( String userId, String dataGroupingType);
}
