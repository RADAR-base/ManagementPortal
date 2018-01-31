package org.radarcns.management.repository;

import org.radarcns.management.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {

    @Query("select authority from Authority authority where authority.name = :authorityName")
    Optional<Authority> findByAuthorityName(@Param("authorityName") String authorityName);

}
