package org.radarcns.management.service.dto;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    private ProjectDTO project;

    private List<RoleDTO> roles;

    private Set<MinimalSourceDetailsDTO> sources = new HashSet<>();

    private Map<String, String> attributes = new HashMap<>();

    public SubjectStatus getStatus() {
        return status;
    }

    public void setStatus(SubjectStatus status) {
        this.status = status;
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

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    /**
     * Gets the login. If no login is present, a new UUID is generated and stored as the login
     * before returning it.
     * @return the login
     */
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

        SubjectDTO subjectDto = (SubjectDTO) o;

        if (id == null || subjectDto.id == null) {
            return false;
        }

        return !Objects.equals(id, subjectDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SubjectDTO{"
                + "id=" + id
                + ", login='" + login + '\''
                + ", externalLink='" + externalLink + '\''
                + ", externalId='" + externalId + '\''
                + ", status=" + status
                + ", project=" + (project == null ? "null" : project.getProjectName())
                + ", attributes=" + attributes + '}';
    }
}
