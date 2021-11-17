package org.radarbase.management.web.rest.criteria;

import org.radarbase.management.web.rest.errors.BadRequestException;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;

public class SubjectCriteria {
    private List<SubjectAuthority> authority = List.of(SubjectAuthority.ROLE_PARTICIPANT);
    private LocalDateCriteriaRange dateOfBirth = null;
    private ZonedDateTimeCriteriaRange enrollmentDate = null;
    private Long groupId = null;
    private String humanReadableIdentifier = null;
    private SubjectCriteriaLast last = null;
    @Min(0)
    private int page = 0;
    @Min(1)
    private int size = 20;
    private List<String> sort;
    private String personName = null;
    private String projectName = null;
    private String externalId = null;
    private String login = null;

    @Transient
    private List<SubjectSortOrder> parsedSort = null;

    public List<SubjectAuthority> getAuthority() {
        return authority;
    }

    public void setAuthority(List<SubjectAuthority> authority) {
        this.authority = authority;
    }

    public CriteriaRange<LocalDate> getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateCriteriaRange dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public CriteriaRange<ZonedDateTime> getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(ZonedDateTimeCriteriaRange enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getHumanReadableIdentifier() {
        return humanReadableIdentifier;
    }

    public void setHumanReadableIdentifier(String humanReadableIdentifier) {
        this.humanReadableIdentifier = humanReadableIdentifier;
    }

    public SubjectCriteriaLast getLast() {
        return last;
    }

    public void setLast(SubjectCriteriaLast last) {
        this.last = last;
    }

    /** Get the criteria paging settings, excluding sorting. */
    @NotNull
    public Pageable getPageable() {
        return PageRequest.of(page, size);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<String> getSort() {
        return sort;
    }

    /** Parse the sort criteria. */
    public List<SubjectSortOrder> getParsedSort() {
        if (this.parsedSort == null) {
            List<String> flatSort = sort != null
                    ? sort.stream()
                            .flatMap(s -> Arrays.stream(s.split(",")))
                            .collect(Collectors.toList())
                    : List.of();

            List<SubjectSortOrder> parsedSort = new ArrayList<>(flatSort.size());

            boolean hasDirection = true;
            for (String part : flatSort) {
                if (!hasDirection) {
                    Optional<Sort.Direction> direction = Sort.Direction.fromOptionalString(
                            part);
                    if (direction.isPresent()) {
                        SubjectSortOrder previous = parsedSort.get(parsedSort.size() - 1);
                        previous.setDirection(direction.get());
                        hasDirection = true;
                        continue;
                    }
                }
                SubjectSortOrder order = new SubjectSortOrder(getSubjectSortBy(part));
                parsedSort.add(order);
                hasDirection = false;
            }

            optimizeSortList(parsedSort);
            this.parsedSort = Collections.unmodifiableList(parsedSort);
        }
        return this.parsedSort;
    }

    /**
     * Remove duplication and redundancy from sort list.
     * @param sort modifiable ordered sort collection.
     */
    private static void optimizeSortList(Collection<SubjectSortOrder> sort) {
        Set<SubjectSortBy> seenSortBy = EnumSet.noneOf(SubjectSortBy.class);
        boolean hasUnique = false;
        Iterator<SubjectSortOrder> iterator = sort.iterator();
        while (iterator.hasNext()) {
            SubjectSortOrder order = iterator.next();
            if (hasUnique || !seenSortBy.add(order.getSortBy())) {
                iterator.remove();
            }
            if (order.getSortBy().isUnique()) {
                hasUnique = true;
            }
        }
        if (!hasUnique) {
            sort.add(new SubjectSortOrder(SubjectSortBy.ID));
        }
    }

    private static SubjectSortBy getSubjectSortBy(String param) {
        return Arrays.stream(SubjectSortBy.values())
                .filter(s -> s.getQueryParam().equalsIgnoreCase(param))
                .findAny()
                .orElseThrow(() -> new BadRequestException(
                        "Cannot convert sort parameter " + param
                                + " to subject property", SUBJECT, ERR_VALIDATION));
    }

    public void setSort(List<String> sort) {
        this.parsedSort = null;
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "SubjectCriteria{" + "authority=" + authority
                + ", dateOfBirth=" + dateOfBirth
                + ", enrollmentDate=" + enrollmentDate
                + ", groupId='" + groupId + '\''
                + ", humanReadableIdentifier='" + humanReadableIdentifier + '\''
                + ", last=" + last
                + ", page=" + page
                + ", sort=" + sort
                + ", personName='" + personName + '\''
                + ", projectName='" + projectName + '\''
                + ", externalId='" + externalId + '\''
                + ", login='" + login + '\''
                + '}';
    }
}
