package org.radarbase.management.repository.filters;

import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.domain.User;
import org.radarbase.management.web.rest.criteria.CriteriaRange;
import org.radarbase.management.web.rest.criteria.SubjectAuthority;
import org.radarbase.management.web.rest.criteria.SubjectCriteria;
import org.radarbase.management.web.rest.criteria.SubjectCriteriaLast;
import org.radarbase.management.web.rest.criteria.SubjectSortBy;
import org.radarbase.management.web.rest.criteria.SubjectSortOrder;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;

public class SubjectSpecification implements Specification<Subject> {
    private final CriteriaRange<LocalDate> dateOfBirth;
    private final CriteriaRange<ZonedDateTime> enrollmentDate;
    private final Long groupId;
    private final String humanReadableIdentifier;
    private final SubjectCriteriaLast last;
    private final String personName;
    private final String projectName;
    private final String externalId;
    private final String subjectId;
    private final List<SubjectSortOrder> sort;
    private final Set<String> authority;
    private final List<String> sortLastValues;

    /**
     * Subject specification based on criteria.
     * @param criteria criteria to use for the specification.
     */
    public SubjectSpecification(SubjectCriteria criteria) {
        this.authority = criteria.getAuthority().stream()
                .map(SubjectAuthority::name)
                .collect(Collectors.toSet());
        this.dateOfBirth = criteria.getDateOfBirth();
        this.enrollmentDate = criteria.getEnrollmentDate();
        this.groupId = criteria.getGroupId();
        this.humanReadableIdentifier = criteria.getHumanReadableIdentifier();
        this.last = criteria.getLast();
        this.personName = criteria.getPersonName();
        this.projectName = criteria.getProjectName();
        this.externalId = criteria.getExternalId();
        this.subjectId = criteria.getLogin();
        this.sort = criteria.getParsedSort();
        if (last != null) {
            this.sortLastValues = this.sort.stream()
                    .map(o -> getLastValue(o.getSortBy()))
                    .toList();
        } else {
            this.sortLastValues = null;
        }
    }

    @Override
    public Predicate toPredicate(@Nullable Root<Subject> root, @Nullable CriteriaQuery<?> query,
            @Nullable CriteriaBuilder builder) {
        if (root == null || query == null || builder == null) {
            return null;
        }
        query.distinct(true);

        root.alias("subject");
        Join<Subject, User> userJoin = root.join("user");
        userJoin.alias("user");

        PredicateBuilder predicates = new PredicateBuilder(builder);

        addRolePredicates(userJoin, predicates);

        predicates.attributeLike(root, "Human-readable-identifier",
                humanReadableIdentifier);
        predicates.likeLower(root.get("externalId"), externalId);

        predicates.equal(root.get("group"), groupId);

        predicates.range(root.get("dateOfBirth"), dateOfBirth);
        predicates.range(root.get("enrollmentDate"), enrollmentDate);

        predicates.likeLower(root.get("personName"), personName);
        predicates.likeLower(userJoin.get("login"), subjectId);

        addContentPredicates(predicates, builder, root, query.getResultType());

        query.orderBy(getSortOrder(root, builder));

        return predicates.toAndPredicate();
    }

    private Predicate filterLastValues(Root<Subject> root, CriteriaBuilder builder) {
        Predicate[] lastPredicates = new Predicate[sort.size()];
        List<Path<String>> paths = new ArrayList<>(sort.size());
        for (SubjectSortOrder order : sort) {
            paths.add(getPropertyPath(order.getSortBy(), root));
        }
        for (int i = 0; i < sort.size(); i++) {
            Predicate[] lastAndPredicates = i > 0 ? new Predicate[i + 1] : null;
            for (int j = 0; j < i; j++) {
                lastAndPredicates[j] = builder.equal(paths.get(j), sortLastValues.get(j));
            }

            SubjectSortOrder order = sort.get(i);
            Predicate currentSort;
            if (order.getDirection().isAscending()) {
                currentSort = builder.greaterThan(paths.get(i), sortLastValues.get(i));
            } else {
                currentSort = builder.lessThan(paths.get(i), sortLastValues.get(i));
            }

            if (lastAndPredicates != null) {
                lastAndPredicates[i] = currentSort;
                lastPredicates[i] = builder.and(lastAndPredicates);
            } else {
                lastPredicates[i] = currentSort;
            }
        }
        if (lastPredicates.length > 1) {
            return builder.or(lastPredicates);
        } else {
            return lastPredicates[0];
        }
    }

    private void addContentPredicates(PredicateBuilder predicates, CriteriaBuilder builder,
            Root<Subject> root, Class<?> queryResult) {
        // Don't add content for count queries.
        if (queryResult == Long.class || queryResult == long.class) {
            return;
        }
        root.fetch("sources", JoinType.LEFT);
        root.fetch("user", JoinType.INNER);

        if (last != null) {
            predicates.add(filterLastValues(root, builder));
        }
    }

    private String getLastValue(SubjectSortBy property) {
        String result = switch (property) {
            case ID -> last.getId();
            case USER_LOGIN -> last.getLogin();
            case EXTERNAL_ID -> last.getExternalId();
        };
        if (property.isUnique() && result == null) {
            throw new BadRequestException("No last value given for sort property " + property,
                    SUBJECT, ERR_VALIDATION);
        }
        return result;
    }


    private Path<String> getPropertyPath(SubjectSortBy property, Root<Subject> root) {
        return switch (property) {
            case ID -> root.get("id");
            case USER_LOGIN -> root.get("user").get("login");
            case EXTERNAL_ID -> root.get("externalId");
        };
    }

    private void addRolePredicates(Join<Subject, User> userJoin, PredicateBuilder predicates) {
        Join<User, Role> rolesJoin = userJoin.join("roles");
        rolesJoin.alias("roles");

        predicates.equal(() -> rolesJoin.get("project").get("projectName"), projectName);
        if (!authority.isEmpty() && authority.size() != SubjectAuthority.values().length) {
            predicates.add(rolesJoin.get("authority").get("name").in(authority));
        }
    }

    private List<Order> getSortOrder(Root<Subject> root,
            CriteriaBuilder builder) {
        return sort.stream()
                .map(order -> {
                    Path<String> path = getPropertyPath(order.getSortBy(), root);
                    if (order.getDirection().isAscending()) {
                        return builder.asc(path);
                    } else {
                        return builder.desc(path);
                    }
                })
                .toList();
    }
}
