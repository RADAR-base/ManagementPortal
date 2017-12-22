package org.radarcns.management.service.dto;


import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A DTO for the Subject entity.
 */
public class SubjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum SubjectStatus {
        DEACTIVATED,    // activated = false, removed = false
        ACTIVATED,      // activated = true,  removed = false
        DISCONTINUED,   // activated = false, removed = true
        INVALID         // activated = true,  removed = true (invalid state, makes no sense)
    }

    public static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    private Long id;

    private String login;

    private String externalLink;

    private String externalId;

    private SubjectStatus status = SubjectStatus.DEACTIVATED;

    private String createdBy;

    private ZonedDateTime createdDate;

    private String lastModifiedBy;

    private ZonedDateTime lastModifiedDate;

    private ProjectDTO project;

    private Set<MinimalSourceDetailsDTO> sources = new HashSet<>();

    private Map<String, String> attributes = new HashMap<>();

    public SubjectStatus getStatus() {
        return status;
    }

    public void setStatus(SubjectStatus status) {
        this.status = status;
    }


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ZonedDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getLogin() {
        if (this.login == null) {
            this.login = UUID.randomUUID().toString();
        }
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Set<MinimalSourceDetailsDTO> getSources() {
        return sources;
    }

    public void setSources(Set<MinimalSourceDetailsDTO> sources) {
        this.sources = sources;
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

        SubjectDTO subjectDTO = (SubjectDTO) o;

        if (id == null || subjectDTO.id == null) {
            return false;
        }

        return !Objects.equals(id, subjectDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SubjectDTO{" +
                "id=" + id +
                ", externalLink='" + externalLink + "'" +
                ", externalId='" + externalId + "'" +
                ", status='" + status + "'" +
                '}';
    }
}
