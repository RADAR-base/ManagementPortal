package org.radarcns.management.repository;

import org.radarcns.management.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by nivethika on 18-5-17.
 */
@SuppressWarnings("unused")
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("select role from Role role inner join role.authority authority where authority.name = :authorityName")
    Role findByAuthorityName(@Param("authorityName") String authorityName);
}
