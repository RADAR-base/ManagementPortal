package org.radarbase.management.repository.filters;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.domain.Organization;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserFilter implements Specification<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserFilter.class);

    private String login;
    private String email;
    private String projectName;
    private String organization;
    private String authority;
    private boolean includeUpperLevels = false;

    @Override
    public Predicate toPredicate(Root<User> root, @Nonnull CriteriaQuery<?> query,
            @Nonnull CriteriaBuilder builder) {
        PredicateBuilder predicates = new PredicateBuilder(builder);
        predicates.likeLower(root.get("login"), login);
        predicates.likeLower(root.get("email"), email);

        filterRoles(predicates, root.join("roles", JoinType.LEFT), query);

        query.distinct(true);
        var result = predicates.toAndPredicate();
        logger.debug("Filtering users by {}", result);
        return result;
    }

    private void filterRoles(PredicateBuilder predicates, Join<User, Role> roleJoin,
            CriteriaQuery<?> query) {
        Stream<RoleAuthority> authoritiesFiltered = Stream.of(RoleAuthority.values())
                .filter(java.util.function.Predicate.not(RoleAuthority::isPersonal));
        boolean allowNoRole = true;

        if (predicates.isValidValue(authority)) {
            String authorityUpper = authority.toUpperCase(Locale.ROOT);
            authoritiesFiltered = authoritiesFiltered
                    .filter(r -> r != null && r.authority().contains(authorityUpper));
            allowNoRole = false;
        }
        List<RoleAuthority> authoritiesAllowed = authoritiesFiltered.collect(Collectors.toList());
        if (authoritiesAllowed.isEmpty()) {
            CriteriaBuilder builder = predicates.getCriteriaBuilder();
            // never match
            predicates.add(builder.isTrue(builder.literal(false)));
            return;
        }

        determineScope(predicates, roleJoin, query, authoritiesAllowed, allowNoRole);
    }

    private void determineScope(
            PredicateBuilder predicates,
            Join<User, Role> roleJoin,
            CriteriaQuery<?> query,
            List<RoleAuthority> authoritiesAllowed,
            boolean allowNoRole) {
        PredicateBuilder authorityPredicates = predicates.newBuilder();

        boolean allowNoRoleInScope = allowNoRole;
        // Is organization admin
        if (predicates.isValidValue(projectName)) {
            allowNoRoleInScope = false;
            // Is project admin
            entitySubquery(RoleAuthority.Scope.PROJECT, roleJoin,
                    query, authorityPredicates, authoritiesAllowed,
                    (b, proj) -> b.likeLower(proj.get("projectName"), projectName));

            // Is organization admin for organization above current project
            if (includeUpperLevels) {
                entitySubquery(RoleAuthority.Scope.ORGANIZATION, roleJoin,
                        query, authorityPredicates, authoritiesAllowed,
                        (b, org) -> b.likeLower(
                                org.join("projects").get("projectName"), projectName));
            }
        } else if (predicates.isValidValue(organization)) {
            allowNoRoleInScope = false;
            entitySubquery(RoleAuthority.Scope.ORGANIZATION, roleJoin,
                    query, authorityPredicates, authoritiesAllowed,
                    (b, org) -> b.likeLower(org.get("name"), organization));
        }

        if (authorityPredicates.isEmpty()) {
            // no project or organization filters applied
            addAllowedAuthorities(authorityPredicates, roleJoin, authoritiesAllowed, null);
        } else if (includeUpperLevels) {
            // is sys admin
            addAllowedAuthorities(authorityPredicates, roleJoin, authoritiesAllowed,
                    RoleAuthority.Scope.GLOBAL);
        }
        if (allowNoRoleInScope) {
            authorityPredicates.isNull(roleJoin.get("id"));
        }

        predicates.add(authorityPredicates.toOrPredicate());
    }

    private boolean addAllowedAuthorities(PredicateBuilder predicates,
            Join<User, Role> roleJoin,
            List<RoleAuthority> authorities,
            RoleAuthority.Scope scope) {

        Stream<RoleAuthority> authorityStream = authorities.stream();
        if (scope != null) {
            authorityStream = authorityStream.filter(r -> r.scope() == scope);
        }
        List<String> authorityNames = authorityStream
                .map(RoleAuthority::authority)
                .collect(Collectors.toList());

        if (!authorityNames.isEmpty()) {
            predicates.in(roleJoin.get("authority").get("name"), authorityNames);
            return true;
        } else {
            return false;
        }
    }

    /** Create a subquery to filter the roles. */
    private void entitySubquery(RoleAuthority.Scope scope,
            Join<User, Role> roleJoin,
            CriteriaQuery<?> query,
            PredicateBuilder predicates,
            List<RoleAuthority> allowedRoles,
            BiConsumer<PredicateBuilder, From<?, ?>> queryMatch) {
        PredicateBuilder authorityPredicates = predicates.newBuilder();

        if (!addAllowedAuthorities(authorityPredicates, roleJoin, allowedRoles, scope)) {
            return;
        }

        Subquery<Long> subQuery = query.subquery(Long.class);
        subQuery.distinct(true);
        Root<?> orgRoot = subQuery.from((Class<?>) switch (scope) {
            case PROJECT -> Project.class;
            case ORGANIZATION -> Organization.class;
            default -> throw new IllegalStateException("Unknown role scope " + scope);
        });
        subQuery.select(orgRoot.get("id"));
        PredicateBuilder subqueryPredicates = predicates.newBuilder();
        queryMatch.accept(subqueryPredicates, orgRoot);
        subQuery.where(subqueryPredicates.toAndPredicate());

        authorityPredicates.in(roleJoin.get( switch (scope) {
            case ORGANIZATION -> "organization";
            case PROJECT -> "project";
            default -> throw new IllegalStateException("Unknown role scope " + scope);
        }).get("id"), subQuery);

        predicates.add(authorityPredicates.toAndPredicate());
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

    public boolean isIncludeUpperLevels() {
        return includeUpperLevels;
    }

    public void setIncludeUpperLevels(boolean includeUpperLevels) {
        this.includeUpperLevels = includeUpperLevels;
    }
}
