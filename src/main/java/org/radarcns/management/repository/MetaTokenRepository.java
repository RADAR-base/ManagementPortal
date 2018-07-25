package org.radarcns.management.repository;

import java.util.Optional;

import org.radarcns.management.domain.MetaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

/**
 * Spring Data JPA repository for the MetaToken entity.
 */
public interface MetaTokenRepository extends JpaRepository<MetaToken, Long>,
    RevisionRepository<MetaToken, Long, Integer> {

    Optional<MetaToken> findOneByTokenName(String tokenName);
}
