package org.radarbase.management.repository.filters;

import org.apache.commons.lang3.StringUtils;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.domain.User;
import org.radarbase.management.web.rest.criteria.SortDirection;
import org.radarbase.management.web.rest.criteria.SubjectCriteria;
import org.radarbase.management.web.rest.criteria.SubjectSortBy;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;


public class SubjectSpecification implements Specification<Subject> {
    private final boolean includeInactive;
    private final LocalDate dateOfBirthFrom;
    private final LocalDate dateOfBirthTo;
    private final ZonedDateTime enrollmentDateFrom;
    private final ZonedDateTime enrollmentDateTo;
    private final String groupName;
    private final String humanReadableIdentifier;
    private final Long lastLoadedId;
    private final String personName;
    private final String projectName;
    private final String externalId;
    private final String subjectId;
    private final SubjectSortBy sortBy;
    private final SortDirection sortDirection;

    /**
     * Subject specification based on criteria.
     * @param criteria criteria to use for the specification.
     */
    public SubjectSpecification(SubjectCriteria criteria) {
        this.includeInactive = criteria.isIncludeInactive();
        this.dateOfBirthFrom = criteria.getDateOfBirthFrom();
        this.dateOfBirthTo = criteria.getDateOfBirthTo();
        this.enrollmentDateFrom = criteria.getEnrollmentDateFrom();
        this.enrollmentDateTo = criteria.getEnrollmentDateTo();
        this.groupName = criteria.getGroupName();
        this.humanReadableIdentifier = criteria.getHumanReadableIdentifier();
        this.lastLoadedId = criteria.getLastLoadedId();
        this.personName = criteria.getPersonName();
        this.projectName = criteria.getProjectName();
        this.externalId = criteria.getExternalId();
        this.subjectId = criteria.getSubjectId();
        this.sortBy = criteria.getSortBy();
        this.sortDirection = criteria.getSortDirection();
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
        List<Predicate> predicates = new ArrayList<>();

        addRolePredicates(builder, userJoin, predicates);

        if (StringUtils.isNotEmpty(humanReadableIdentifier)) {
            MapJoin<Subject, String, String> attributesJoin =
                    root.joinMap("attributes", JoinType.LEFT);
            predicates.add(builder.and(
                    builder.equal(attributesJoin.key(), "Human-readable-identifier"),
                    builder.like(attributesJoin.value(),
                            "%" + humanReadableIdentifier + "%")));
        }
        if (StringUtils.isNotEmpty(externalId)) {
            predicates.add(builder.like(root.get("externalId"), "%" + externalId + "%"));
        }
        if (StringUtils.isNotEmpty(groupName)) {
            predicates.add(builder.equal(root.get("group"), groupName));
        }

        addDateOfBirthPredicates(root, builder, predicates);
        addEnrollmentDatePredicates(root, builder, predicates);

        if (StringUtils.isNotEmpty(personName)) {
            predicates.add(builder
                    .like(root.get("personName"), "%" + personName + "%"));
        }
        if (StringUtils.isNotEmpty(subjectId)) {
            predicates.add(builder.like(userJoin.get("login"), "%" + subjectId + "%"));
        }

        addContentPredicates(root, builder, query.getResultType(), predicates);

        query.orderBy(getSortOrder(root, builder));

        return builder.and(predicates.toArray(new Predicate[0]));
    }

    private void addContentPredicates(Root<Subject> root, CriteriaBuilder builder,
            Class<?> queryResult, List<Predicate> predicates) {
        if (queryResult == Long.class || queryResult == long.class) {
            return;
        }
        root.fetch("sources", JoinType.LEFT);
        root.fetch("user", JoinType.LEFT);

        if (lastLoadedId != null) {
            if (sortBy == SubjectSortBy.ID && sortDirection == SortDirection.DESC) {
                predicates.add(builder.lessThan(root.get("id"), lastLoadedId));
            } else {
                predicates.add(builder.greaterThan(root.get("id"), lastLoadedId));
            }
        }
    }

    private void addRolePredicates(CriteriaBuilder builder, Join<Subject, User> userJoin,
            List<Predicate> predicates) {
        Join<User, Role> rolesJoin = userJoin.join("roles");
        rolesJoin.alias("roles");

        if (StringUtils.isNotEmpty(projectName)) {
            predicates.add(builder.equal(
                    rolesJoin.get("project").get("projectName"),
                    projectName));
        }
        if (!includeInactive) {
            predicates.add(rolesJoin.get("authority").get("name")
                    .in(Collections.singletonList(PARTICIPANT)));
        }
    }

    private void addDateOfBirthPredicates(
            Root<Subject> root,
            CriteriaBuilder builder,
            List<Predicate> predicates) {
        if (dateOfBirthFrom != null
                && dateOfBirthFrom.equals(dateOfBirthTo)) {
            predicates.add(builder.equal(root.get("dateOfBirth"), dateOfBirthTo));
        } else {
            if (dateOfBirthFrom != null
                    && dateOfBirthTo != null
                    && dateOfBirthFrom.compareTo(dateOfBirthTo) < 0) {
                throw new BadRequestException(
                        "Date of birth start range may not precede date of birth end range.",
                        SUBJECT, ERR_VALIDATION);
            }
            if (dateOfBirthFrom != null) {
                predicates.add(builder
                        .greaterThanOrEqualTo(root.get("dateOfBirth"), dateOfBirthFrom));
            }
            if (dateOfBirthTo != null) {
                predicates.add(builder
                        .lessThanOrEqualTo(root.get("dateOfBirth"), dateOfBirthTo));
            }
        }
    }

    private void addEnrollmentDatePredicates(
            Root<Subject> root,
            CriteriaBuilder builder,
            List<Predicate> predicates) {
        if (enrollmentDateFrom != null
                && enrollmentDateTo != null
                && enrollmentDateFrom.compareTo(enrollmentDateTo) < 0) {
            throw new BadRequestException(
                    "Enrollment date start range may not precede enrollment date end range.",
                    SUBJECT, ERR_VALIDATION);
        }
        if (enrollmentDateFrom != null) {
            predicates.add(builder
                    .greaterThanOrEqualTo(root.get("enrollmentDate"), enrollmentDateFrom));
        }
        if (enrollmentDateTo != null) {
            predicates.add(builder
                    .lessThan(root.get("enrollmentDate"), enrollmentDateTo));
        }
    }

    private List<Order> getSortOrder(Root<Subject> root, CriteriaBuilder builder) {
        Path<?> sortingPath;
        switch (sortBy) {
            case ID:
                sortingPath = root.get("id");
                break;
            case EXTERNAL_ID:
                sortingPath = root.get("externalId");
                break;
            case USER_ACTIVATED:
                sortingPath = root.get("user").get("activated");
                break;
            case USER_LOGIN:
                sortingPath = root.get("user").get("login");
                break;
            default:
                throw new IllegalStateException("Cannot sort on unknown sort parameter " + sortBy);
        }

        List<Order> orderList = new ArrayList<>();
        if (sortDirection == SortDirection.DESC) {
            orderList.add(builder.desc(sortingPath));
        } else {
            orderList.add(builder.asc(sortingPath));
        }
        // We need to guarantee that the sorting is stable to make pagination work
        if (sortBy != SubjectSortBy.ID) {
            orderList.add(builder.asc(root.get("id")));
        }
        return orderList;
    }
}
