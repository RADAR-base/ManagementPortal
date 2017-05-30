package org.radarcns.management.service.dto;

import java.util.HashSet;
import java.util.Set;
import org.radarcns.management.domain.Project;

/**
 * Created by nivethika on 23-5-17.
 */
public class RoleDTO {
    private Long id;

    private Set<UserDTO> users = new HashSet<>();

    private Project project;

    private String authorityName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(Set<UserDTO> users) {
        this.users = users;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }
}
