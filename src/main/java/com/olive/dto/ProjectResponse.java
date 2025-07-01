package com.olive.dto;

public class ProjectResponse {
    private Long id;
    private String projectName;
    private String description;

    public ProjectResponse(Long id, String projectName, String description) {
        this.id = id;
        this.projectName = projectName;
        this.description = description;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
