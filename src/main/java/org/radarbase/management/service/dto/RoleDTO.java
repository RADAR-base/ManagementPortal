package org.radarbase.management.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Created by nivethika on 23-5-17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO {
    private Long id;

    private Long organizationId;

    private String organizationName;

    private Long projectId;

    private String projectName;

    private String authorityName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return "RoleDTO{" + "id=" + id
                + ", organizationId=" + organizationId
                + ", projectId=" + projectId
                + ", authorityName='" + authorityName + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RoleDTO roleDto = (RoleDTO) o;

        return Objects.equals(id, roleDto.id)
                && Objects.equals(organizationId, roleDto.organizationId)
                && Objects.equals(organizationName, roleDto.organizationName)
                && Objects.equals(projectId, roleDto.projectId)
                && Objects.equals(projectName, roleDto.projectName)
                && Objects.equals(authorityName, roleDto.authorityName);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
