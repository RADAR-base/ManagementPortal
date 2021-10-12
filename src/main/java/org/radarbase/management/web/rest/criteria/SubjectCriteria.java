package org.radarbase.management.web.rest.criteria;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class SubjectCriteria {
    private boolean includeInactive = false;
    private LocalDate dateOfBirthFrom = null;
    private LocalDate dateOfBirthTo = null;
    private ZonedDateTime enrollmentDateFrom = null;
    private ZonedDateTime enrollmentDateTo = null;
    private String groupName = null;
    private String humanReadableIdentifier = null;
    private Long lastLoadedId = null;
    @NotNull
    @Min(1)
    private Integer pageSize = 10;
    private String personName = null;
    private String projectName = null;
    private String externalId = null;
    private String subjectId = null;
    @NotNull
    private SubjectSortBy sortBy = SubjectSortBy.ID;
    @NotNull
    private SortDirection sortDirection = SortDirection.ASC;

    public boolean isIncludeInactive() {
        return includeInactive;
    }

    public void setIncludeInactive(boolean includeInactive) {
        this.includeInactive = includeInactive;
    }

    public LocalDate getDateOfBirthFrom() {
        return dateOfBirthFrom;
    }

    public void setDateOfBirthFrom(LocalDate dateOfBirthFrom) {
        this.dateOfBirthFrom = dateOfBirthFrom;
    }

    public LocalDate getDateOfBirthTo() {
        return dateOfBirthTo;
    }

    public void setDateOfBirthTo(LocalDate dateOfBirthTo) {
        this.dateOfBirthTo = dateOfBirthTo;
    }

    public ZonedDateTime getEnrollmentDateFrom() {
        return enrollmentDateFrom;
    }

    public void setEnrollmentDateFrom(ZonedDateTime enrollmentDateFrom) {
        this.enrollmentDateFrom = enrollmentDateFrom;
    }

    public ZonedDateTime getEnrollmentDateTo() {
        return enrollmentDateTo;
    }

    public void setEnrollmentDateTo(ZonedDateTime enrollmentDateTo) {
        this.enrollmentDateTo = enrollmentDateTo;
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

    public Long getLastLoadedId() {
        return lastLoadedId;
    }

    public void setLastLoadedId(Long lastLoadedId) {
        this.lastLoadedId = lastLoadedId;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public SubjectSortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(SubjectSortBy sortBy) {
        this.sortBy = sortBy;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }
}
