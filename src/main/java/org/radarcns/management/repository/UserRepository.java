package org.radarcns.management.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.radarcns.management.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(ZonedDateTime dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findOneWithRolesByLogin(String login);

    Page<User> findAllByLoginNot(Pageable pageable, String login);

}
