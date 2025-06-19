package com.olive.dto;

import java.util.List;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private List<Long> projectIds; // UPDATED: List of project IDs
    private List<String> projectNames; // NEW: List of project names (comma-separated for display)

    public UserResponse(Long id, String fullName, String email, String role, List<Long> projectIds, List<String> projectNames) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.projectIds = projectIds;
        this.projectNames = projectNames;
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

    // UPDATED: Getter and Setter for projectIds (List<Long>)
    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }

    // NEW: Getter and Setter for projectNames (List<String>)
    public List<String> getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(List<String> projectNames) {
        this.projectNames = projectNames;
    }
}
