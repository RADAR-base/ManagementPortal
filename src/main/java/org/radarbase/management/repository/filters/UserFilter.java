package org.radarbase.management.repository.filters;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.User;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserFilter implements Specification<User> {
    private String login;
    private String email;
    private String projectName;
    private String organization;
    private String authority;

    @Override
    public Predicate toPredicate(Root<User> root, @Nonnull CriteriaQuery<?> query,
            @Nonnull CriteriaBuilder builder) {
        PredicateBuilder predicates = new PredicateBuilder(builder);
        predicates.likeLower(root.get("login"), login);
        predicates.likeLower(root.get("email"), email);

        filterRoles(predicates, root.join("roles"));

        query.distinct(true);
        return predicates.toAndPredicate();
    }

    private void filterRoles(PredicateBuilder predicates, Join<User, Role> roleJoin) {
        RoleAuthority.Scope scope = determineScope(predicates, roleJoin);

        Join<Role, Authority> authorityJoin = roleJoin.join("authority");
        if (predicates.isValidValue(authority)) {
            predicates.likeLower(authorityJoin.get("name"), authority);
        } else {
            predicates.add(authorityJoin.get("name")
                    .in(Stream.of(RoleAuthority.values())
                            .filter(scope == null
                                    ? r -> !r.isPersonal()
                                    : r -> r.scope() == scope && !r.isPersonal())
                            .map(RoleAuthority::authority)
                            .collect(Collectors.toList())));
        }
    }

    private RoleAuthority.Scope determineScope(
            PredicateBuilder predicates,
            Join<User, Role> roleJoin) {
        if (predicates.isValidValue(projectName)) {
            predicates.likeLower(
                    () -> roleJoin.join("project").get("projectName"), projectName);
            return RoleAuthority.Scope.PROJECT;
        } else if (predicates.isValidValue(organization)) {
            predicates.likeLower(
                    () -> roleJoin.join("organization").get("name"), organization);
            return RoleAuthority.Scope.ORGANIZATION;
        } else {
            return null;
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
