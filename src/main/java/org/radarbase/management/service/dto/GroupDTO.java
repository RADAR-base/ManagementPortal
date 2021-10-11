package org.radarbase.management.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Group entity.
 */
public class GroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long projectId;

    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupDTO groupDto = (GroupDTO) o;

        if (id == null || groupDto.id == null) {
            return false;
        }

        return Objects.equals(id, groupDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "GroupDTO{"
                + "id=" + id
                + ", name='" + name + "'"
                + '}';
    }
}
