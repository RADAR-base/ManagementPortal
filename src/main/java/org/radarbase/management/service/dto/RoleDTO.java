package org.radarbase.management.service.dto;

import java.util.Objects;

/**
 * Created by nivethika on 23-5-17.
 */
public class RoleDTO {

    private Long id;

    private Long projectId;

    private String projectName;

    private String authorityName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
                && Objects.equals(projectId, roleDto.projectId)
                && Objects.equals(projectName, roleDto.projectName)
                && Objects.equals(authorityName, roleDto.authorityName);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
