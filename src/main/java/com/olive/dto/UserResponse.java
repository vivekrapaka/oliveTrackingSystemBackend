// backend/src/main/java/com/olive/dto/UserResponse.java
package com.olive.dto;

import java.util.List;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private List<Long> projectIds;
    private List<String> projectNames;

    public UserResponse(Long id, String fullName, String email, String role, List<Long> projectIds, List<String> projectNames) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.projectIds = projectIds;
        this.projectNames = projectNames;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public List<String> getProjectNames() {
        return projectNames;
    }

    // Setters (if needed, DTOs are often immutable after creation)
    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }

    public void setProjectNames(List<String> projectNames) {
        this.projectNames = projectNames;
    }
}
