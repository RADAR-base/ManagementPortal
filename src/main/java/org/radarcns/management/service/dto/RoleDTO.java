package org.radarcns.management.service.dto;

import java.util.HashSet;
import java.util.Set;
import org.radarcns.management.domain.Project;

/**
 * Created by nivethika on 23-5-17.
 */
public class RoleDTO {
    private Long id;

    private Long projectId;

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
}
