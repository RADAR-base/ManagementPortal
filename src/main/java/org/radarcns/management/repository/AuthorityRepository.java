package org.radarcns.management.repository;

import java.util.List;
import org.radarcns.management.domain.Authority;

import org.radarcns.management.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {

    @Query("select authority from Authority authority where authority.name = :authorityName")
    Authority findByAuthorityName(@Param("authorityName") String authorityName);

    List<Authority> findAllByNameNotIn(String name);
}
