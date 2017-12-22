package org.radarcns.management.repository;

import java.util.List;
import java.util.Optional;
import org.radarcns.management.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by nivethika on 18-5-17.
 */
@SuppressWarnings("unused")
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("select role from Role role inner join role.authority authority"
            + " where authority.name = :authorityName")
    List<Role> findRolesByAuthorityName(@Param("authorityName") String authorityName);

    @Query("select distinct role from Role role left join fetch role.authority")
    List<Role> findAllWithEagerRelationships();

    @Query("select role from Role role left join fetch role.authority "
            + "where role.id =:id")
    Role findOneWithEagerRelationships(@Param("id") Long id);

    Role findOneByProjectIdAndAuthorityName(@Param("projectId") Long projectId,
            @Param("authorityName") String authorityName);

    @Query("select role from Role role join role.authority join role.project "
            + "where role.project.projectName = :projectName "
            + "and role.authority.name = :authorityName")
    Optional<Role> findOneByProjectNameAndAuthorityName(@Param("projectName") String projectName,
            @Param("authorityName") String authorityName);

    @Query("select role from Role role left join fetch role.authority "
            + "where role.project.projectName = :projectName")
    List<Role> findAllRolesByProjectName(@Param("projectName") String projectName);
}
