package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.envers.Audited;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.domain.enumeration.ProjectStatus;
import org.radarcns.management.domain.support.AbstractEntityListener;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A Project.
 */
@Entity
@Audited
@Table(name = "project")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners({AbstractEntityListener.class})
public class Project extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.ENTITY_ID_REGEX)
    @Column(name = "project_name", nullable = false, unique = true)
    private String projectName;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "jhi_organization")
    private String organization;

    @NotNull
    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status")
    private ProjectStatus projectStatus;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "project_source_type",
            joinColumns = @JoinColumn(name = "projects_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "source_types_id", referencedColumnName = "id"))
    private Set<SourceType> sourceTypes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @CollectionTable(name = "project_metadata", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> attributes = new HashMap<>();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public Project projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public Project description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganization() {
        return organization;
    }

    public Project organization(String organization) {
        this.organization = organization;
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getLocation() {
        return location;
    }

    public Project location(String location) {
        this.location = location;
        return this;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public Project startDate(ZonedDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public Project projectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
        return this;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public Project endDate(ZonedDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public Set<SourceType> getSourceTypes() {
        return sourceTypes;
    }

    public Project sourceTypes(Set<SourceType> sourceTypes) {
        this.sourceTypes = sourceTypes;
        return this;
    }

    /**
     * Add a source type to this project.
     *
     * @param sourceType the source type to add
     * @return this project
     */
    public Project addSourceType(SourceType sourceType) {
        this.sourceTypes.add(sourceType);
        sourceType.getProjects().add(this);
        return this;
    }

    /**
     * Remove a source type from this project.
     *
     * @param sourceType the source type to remove
     * @return this project
     */
    public Project removeSourceType(SourceType sourceType) {
        this.sourceTypes.remove(sourceType);
        sourceType.getProjects().remove(this);
        return this;
    }

    public void setSourceTypes(Set<SourceType> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        if (project.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Project{"
                + "id=" + id
                + ", projectName='" + projectName + "'"
                + ", description='" + description + "'"
                + ", organization='" + organization + "'"
                + ", location='" + location + "'"
                + ", startDate='" + startDate + "'"
                + ", projectStatus='" + projectStatus + "'"
                + ", endDate='" + endDate + "'"
                + "}";
    }
}
