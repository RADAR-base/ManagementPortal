package org.radarbase.management.service.dto;

/**
 * Created by nivethika on 21-6-17.
 */
public class MinimalProjectDetailsDTO {

    private Long id;

    private String projectName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
