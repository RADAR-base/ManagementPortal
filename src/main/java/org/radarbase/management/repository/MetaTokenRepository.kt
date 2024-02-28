package org.radarbase.management.repository

import org.radarbase.management.domain.MetaToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

/**
 * Spring Data JPA repository for the MetaToken entity.
 */
interface MetaTokenRepository : JpaRepository<MetaToken, Long>, RevisionRepository<MetaToken, Long, Int> {
    fun findOneByTokenName(tokenName: String?): MetaToken?

    @Query(
        "select metaToken from MetaToken metaToken "
                + "where (metaToken.fetched = true and metaToken.persistent = false)"
                + " or metaToken.expiryDate < :time"
    )
    fun findAllByFetchedOrExpired(@Param("time") time: Instant?): List<MetaToken>
}
