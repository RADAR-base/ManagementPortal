package org.radarbase.management.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Group entity.
 */
public class GroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String groupName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
                + ", groupName='" + groupName + "'"
                + '}';
    }
}
