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

        RoleAuthority.Scope scope;

        Join<User, Role> roleJoin = root.join("roles");
        if (predicates.isValidValue(projectName)) {
            scope = RoleAuthority.Scope.PROJECT;
            predicates.likeLower(
                    () -> roleJoin.join("project").get("projectName"), projectName);
        } else if (predicates.isValidValue(organization)) {
            scope = RoleAuthority.Scope.ORGANIZATION;
            predicates.likeLower(
                    () -> roleJoin.join("organization").get("name"), organization);
        } else {
            scope = null;
        }

        Join<Role, Authority> authorityJoin = roleJoin.join("authority");
        if (predicates.isValidValue(authority)) {
            predicates.equal(authorityJoin.get("name"), authority);
        } else {
            predicates.add(authorityJoin.get("name")
                    .in(Stream.of(RoleAuthority.values())
                            .filter(scope == null
                                    ? r -> !r.isPersonal()
                                    : r -> r.scope() == scope && !r.isPersonal())
                            .collect(Collectors.toList())));
        }

        query.distinct(true);
        return predicates.toAndPredicate();
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
