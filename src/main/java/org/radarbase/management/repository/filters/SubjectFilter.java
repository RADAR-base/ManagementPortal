package org.radarbase.management.repository.filters;

import static org.radarbase.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;

import org.apache.commons.lang3.StringUtils;
import org.radarbase.management.domain.Role;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.domain.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SubjectFilter implements Specification<Subject> {
    private boolean includeInactive = false;
    private LocalDate dateOfBirthFrom = null;
    private LocalDate dateOfBirthTo = null;
    private ZonedDateTime enrollmentDateFrom = null;
    private ZonedDateTime enrollmentDateTo = null;
    private String groupName = null;
    private Long lastLoadedId = null;
    private Integer pageSize = 10;
    private String personName = null;
    private String projectName = null;
    private String externalId = null;
    private String subjectId = null;
    private SubjectSortBy sortBy = SubjectSortBy.ID;
    private SubjectSortDirection sortDirection = SubjectSortDirection.ASC;
    
    @Override
    public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        query.distinct(true);

        root.alias("subject");
        Class<?> queryResult = query.getResultType();
        boolean isCountQuery = queryResult == Long.class || queryResult == long.class;
        if (!isCountQuery) {
            root.fetch("sources", JoinType.LEFT);
            root.fetch("user", JoinType.LEFT);
        }
        Join<Subject, User> userJoin = root.join("user");
        userJoin.alias("user");
        Join<User, Role> rolesJoin = userJoin.join("roles");
        rolesJoin.alias("roles");

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotEmpty(projectName)) {
            Predicate filterProject = builder.equal(
                rolesJoin.get("project").get("projectName"),
                projectName);
            predicates.add(filterProject);
        }
        List<String> authorities = includeInactive
            ? Arrays.asList(PARTICIPANT, INACTIVE_PARTICIPANT)
            : Collections.singletonList(PARTICIPANT);
        Predicate filterAuthorities =
            rolesJoin.get("authority").get("name").in(authorities);
        predicates.add(filterAuthorities);

        if (StringUtils.isNotEmpty(externalId)) {
            predicates.add(builder.equal(root.get("externalId"), externalId));
        }
        if (StringUtils.isNotEmpty(groupName)) {
            predicates.add(builder.equal(root.get("group"), groupName));
        }
        if (dateOfBirthFrom != null) {
            predicates.add(builder
                .greaterThanOrEqualTo(root.get("dateOfBirth"), dateOfBirthFrom));
        }
        if (dateOfBirthTo != null) {
            predicates.add(builder
                .lessThan(root.get("dateOfBirth"), dateOfBirthTo));
        }
        if (enrollmentDateFrom != null) {
            predicates.add(builder
                .greaterThanOrEqualTo(root.get("enrollmentDate"), enrollmentDateFrom));
        }
        if (enrollmentDateTo != null) {
            predicates.add(builder
                .lessThan(root.get("enrollmentDate"), enrollmentDateTo));
        }
        if (StringUtils.isNotEmpty(personName)) {
            predicates.add(builder
                .like(root.get("personName"), "%" + personName + "%"));
        }
        if (StringUtils.isNotEmpty(subjectId)) {
            predicates.add(builder.equal(userJoin.get("login"), subjectId));
        }

        if (!isCountQuery && lastLoadedId != null) {
            if (sortBy == SubjectSortBy.ID && sortDirection == SubjectSortDirection.DESC) {
                predicates.add(builder.lessThan(root.get("id"), lastLoadedId));
            } else {
                predicates.add(builder.greaterThan(root.get("id"), lastLoadedId));
            }
        }

        List<Order> orderList = new ArrayList<>();

        Path<?> sortingPath = null;
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
            case UNKNOWN:
                throw new IllegalStateException("BUG: filter was not validated");
            default:
                throw new IllegalStateException("BUG: sortBy was not initalized");
        }
        if (sortDirection == SubjectSortDirection.DESC) {
            orderList.add(builder.desc(sortingPath));
        } else {
            orderList.add(builder.asc(sortingPath));
        }
        // We need to guarantee that the sorting is stable to make pagination work
        if (sortBy != SubjectSortBy.ID) {
            orderList.add(builder.asc(root.get("id")));
        }
        
        query.orderBy(orderList);
 
        return builder.and(predicates.toArray(new Predicate[0]));
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public LocalDate getDateOfBirthTo() {
        return dateOfBirthTo;
    }

    public void setDateOfBirthTo(String date) {
        this.dateOfBirthTo = LocalDate.parse(date, dateTimeFormatter);
    }

    public LocalDate getDateOfBirthFrom() {
        return dateOfBirthFrom;
    }

    public void setDateOfBirthFrom(String date) {
        this.dateOfBirthFrom = LocalDate.parse(date, dateTimeFormatter);
    }

    public ZonedDateTime getEnrollmentDateTo() {
        return enrollmentDateTo;
    }

    public void setEnrollmentDateTo(String datetime) {
        this.enrollmentDateTo = ZonedDateTime.parse(datetime, dateTimeFormatter);
    }

    public ZonedDateTime getEnrollmentDateFrom() {
        return enrollmentDateFrom;
    }

    public void setEnrollmentDateFrom(String datetime) {
        this.enrollmentDateFrom = ZonedDateTime.parse(datetime, dateTimeFormatter);
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getWithInactiveParticipants() {
        return this.includeInactive;
    }

    public void setWithInactiveParticipants(Boolean value) {
        this.includeInactive = Boolean.TRUE.equals(value);
    }

    public void setLastLoadedId(Long lastLoadedId) {
        this.lastLoadedId = lastLoadedId;
    }

    public Long getLastLoadedId() {
        return this.lastLoadedId;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectId() {
        return this.subjectId;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = SubjectSortBy.fromString(sortBy);
    }

    public SubjectSortBy getSortBy() {
        return this.sortBy;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = SubjectSortDirection.fromString(sortDirection);
    }

    public SubjectSortDirection getSortDirection() {
        return this.sortDirection;
    }

    /**
     * Return an error message to be used for a 404 response
     * in case of e.g. an unknown sortBy key.
     */
    public String getFilterValidationError() {
        if (pageSize == null || pageSize <= 0) {
            return "Unexpected pageSize value";
        }
        if (sortBy == SubjectSortBy.UNKNOWN) {
            return "Unexpected sortBy value";
        }
        if (sortDirection == SubjectSortDirection.UNKNOWN) {
            return "Unexpected sortDirection value";
        }
        return null;
    }

    private static DateTimeFormatter dateTimeFormatter;
    static {
        // This lenient formatter handles
        // both date and datetime strings (zoned and unzoned)
        // in order to be easy for JS clients
        // (i.e. it accepts ISO-formatted dates, unlike default LocalDate parser)
        // and for humans (i.e. you can type '2020-10-10') to use.
        dateTimeFormatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .optionalStart()
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalStart()
            .appendOffsetId()
            .optionalEnd()
            .optionalEnd()
            .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
            .parseDefaulting(MINUTE_OF_HOUR, MINUTE_OF_HOUR.range().getMinimum())
            .parseDefaulting(SECOND_OF_MINUTE, SECOND_OF_MINUTE.range().getMinimum())
            .parseDefaulting(NANO_OF_SECOND, NANO_OF_SECOND.range().getMinimum())
            .toFormatter()
            .withZone(ZoneId.of("UTC"))
            .withResolverStyle(ResolverStyle.LENIENT);
    }

    public enum SubjectSortBy {
        ID("id"),
        EXTERNAL_ID("externalId"),
        USER_LOGIN("user.login"),
        USER_ACTIVATED("user.activated"),
        UNKNOWN(null);

        private final String key;

        SubjectSortBy(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public static SubjectSortBy fromString(String text) {
            if (StringUtils.isEmpty(text)) {
                return ID;
            }
            for (SubjectSortBy s : SubjectSortBy.values()) {
                if (s == UNKNOWN) continue;
                if (s.key.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            return UNKNOWN;
        }
    }

    public enum SubjectSortDirection {
        ASC("asc"),
        DESC("desc"),
        UNKNOWN(null);

        private final String key;

        SubjectSortDirection(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public static SubjectSortDirection fromString(String text) {
            if (StringUtils.isEmpty(text)) {
                return ASC;
            }
            for (SubjectSortDirection d : SubjectSortDirection.values()) {
                if (d == UNKNOWN) continue;
                if (d.key.equalsIgnoreCase(text)) {
                    return d;
                }
            }
            return UNKNOWN;
        }
    }
}
