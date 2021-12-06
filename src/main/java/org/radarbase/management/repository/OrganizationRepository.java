package org.radarbase.management.repository;

import java.util.List;
import java.util.Optional;

import org.radarbase.management.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Organization entity.
 */
@RepositoryDefinition(domainClass = Organization.class, idClass = Long.class)
public interface OrganizationRepository extends JpaRepository<Organization, Long>,
        RevisionRepository<Organization, Long, Integer> {

    @Query("select org from Organization org "
            + "where org.name = :name")
    Optional<Organization> findOneByName(@Param("name") String name);

    @Query("select distinct org from Organization org left join fetch org.projects project "
            + "where project.projectName in (:projectNames)")
    List<Organization> findAllByProjectNames(@Param("projectNames") List<String> projectNames);
}
