package org.radarcns.management.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.radarcns.management.domain.MetaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the MetaToken entity.
 */
public interface MetaTokenRepository extends JpaRepository<MetaToken, Long>,
        RevisionRepository<MetaToken, Long, Integer> {

    Optional<MetaToken> findOneByTokenName(String tokenName);

    @Query("select metaToken from MetaToken metaToken "
            + "where metaToken.fetched =:fetched or metaToken.expiryDate < :time")
    List<MetaToken> findAllByFetchedOrExpired(@Param("fetched") Boolean fetched,
            @Param("time")Instant time);
}
