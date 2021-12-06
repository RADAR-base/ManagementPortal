package org.radarbase.management.service.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the Organization entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private String location;

    private List<ProjectDTO> projects;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var orgDto = (OrganizationDTO) o;
        return Objects.equals(name, orgDto.name)
                && Objects.equals(description, orgDto.description)
                && Objects.equals(location, orgDto.location);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "OrganizationDTO{"
                + "id=" + id
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", location='" + location + "'"
                + '}';
    }
}
