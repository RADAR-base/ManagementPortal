package org.radarbase.management.repository;

import org.radarbase.management.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 */
@RepositoryDefinition(domainClass = User.class, idClass = Long.class)
public interface UserRepository extends JpaRepository<User, Long>,
        RevisionRepository<User, Long, Integer>, JpaSpecificationExecutor<User> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivated(boolean activated);

    @Query("select user from User user "
            + "left join fetch user.roles roles where "
            + "roles.authority.name not in :authorities "
            + "and user.activated= :activated")
    List<User> findAllByActivatedAndAuthoritiesNot(@Param("activated") boolean activated,
            @Param("authorities") List<String> authorities);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findOneWithRolesByLogin(String login);

    Page<User> findAllByLoginNot(Pageable pageable, String login);

}
