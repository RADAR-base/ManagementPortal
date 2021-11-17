package org.radarbase.management.repository.filters;

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

import static org.radarbase.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;

public class UserFilter implements Specification<User> {
    private String login;
    private String email;
    private String projectName;
    private String authority;

    @Override
    public Predicate toPredicate(Root<User> root, @Nonnull CriteriaQuery<?> query,
            @Nonnull CriteriaBuilder builder) {
        PredicateBuilder predicates = new PredicateBuilder(builder);
        Join<User, Role> roleJoin = root.join("roles");
        Join<Role, Authority> authorityJoin = roleJoin.join("authority");
        predicates.add(builder.not(authorityJoin.get("name")
                .in(PARTICIPANT, INACTIVE_PARTICIPANT)));

        predicates.likeLower(root.get("login"), login);
        predicates.likeLower(root.get("email"), email);
        predicates.likeLower(authorityJoin.get("name"), authority);

        predicates.likeLower(
                () -> roleJoin.join("project").get("projectName"), projectName);

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

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
