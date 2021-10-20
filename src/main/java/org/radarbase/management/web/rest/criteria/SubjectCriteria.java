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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;

public class SubjectCriteria {
    private List<SubjectAuthority> authority = List.of(SubjectAuthority.ROLE_PARTICIPANT);
    private LocalDateCriteriaRange dateOfBirth = null;
    private ZonedDateTimeCriteriaRange enrollmentDate = null;
    private String groupName = null;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
            boolean hasUniqueField = false;
            List<SubjectSortOrder> parsedSort;

            if (sort != null) {
                parsedSort = new ArrayList<>(sort.size());

                List<String> flatSort = sort.stream()
                        .flatMap(s -> Arrays.stream(s.split(",")))
                        .collect(Collectors.toList());

                int index = 0;

                while (index < flatSort.size()) {
                    String part = flatSort.get(index);
                    SubjectSortBy sortBy = Arrays.stream(SubjectSortBy.values())
                            .filter(s -> s.getQueryParam().equalsIgnoreCase(part))
                            .findAny()
                            .orElseThrow(() -> new BadRequestException(
                                    "Cannot convert sort property " + part
                                            + " to subject property", SUBJECT, ERR_VALIDATION));

                    Optional<Sort.Direction> direction = (index + 1 < flatSort.size())
                            ? Sort.Direction.fromOptionalString(flatSort.get(index + 1))
                            : Optional.empty();

                    SubjectSortOrder order;
                    if (direction.isPresent()) {
                        index += 2;
                        order = new SubjectSortOrder(sortBy, direction.get());
                    } else {
                        index++;
                        order = new SubjectSortOrder(sortBy);
                    }
                    if (parsedSort.stream().noneMatch(o -> o.getSortBy() == sortBy)) {
                        parsedSort.add(order);
                        // No need to sort beyond a unique element: the order will not change.
                        if (sortBy.isUnique()) {
                            hasUniqueField = true;
                            break;
                        }
                    }
                }
            } else {
                parsedSort = new ArrayList<>(1);
            }

            // Ensure that the result has a fully defined order.
            if (!hasUniqueField) {
                parsedSort.add(new SubjectSortOrder(SubjectSortBy.ID));
            }

            this.parsedSort = Collections.unmodifiableList(parsedSort);
        }
        return this.parsedSort;
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
                + ", groupName='" + groupName + '\''
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
