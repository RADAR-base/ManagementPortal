package org.radarbase.management.repository;

import org.radarbase.management.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Authority entity.
 */
@RepositoryDefinition(domainClass = Authority.class, idClass = String.class)
public interface AuthorityRepository extends JpaRepository<Authority, String>,
        RevisionRepository<Authority, String, Integer> {

    @Query("select authority from Authority authority where authority.name = :authorityName")
    Optional<Authority> findByAuthorityName(@Param("authorityName") String authorityName);
}
