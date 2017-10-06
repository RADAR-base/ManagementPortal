package org.radarcns.management.repository;

import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;

import java.time.ZonedDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(ZonedDateTime dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    User findOneWithAuthoritiesById(Long id);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findOneWithRolesByLogin(String login);

    Page<User> findAllByLoginNot(Pageable pageable, String login);

    @Query("select user from User user join user.roles roles "
        + " where roles.project.id = :projectId "
        + " and roles.authority.name = :authority")
    Page<User> findAllByProjectIdAndAuthority(Pageable pageable, @Param("projectId") Long projectId,
        @Param("authority") String authority);

    @Query("select user from User user join user.roles roles "
        + " where roles.authority.name = :authority")
    Page<User> findAllByAuthority(Pageable pageable, @Param("authority") String authority);

    @Query("select user from User user join user.roles roles "
        + " where roles.project.id = :projectId ")
    Page<User> findAllByProjectId(Pageable pageable, Long projectId);
}
