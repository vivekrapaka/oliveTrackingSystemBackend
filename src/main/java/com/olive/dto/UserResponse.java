package com.olive.dto;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private Long projectId;
    private String projectName; // To show the project name for frontend display

    public UserResponse(Long id, String fullName, String email, String role, Long projectId, String projectName) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
