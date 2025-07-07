// backend/src/main/java/com/olive/dto/AuthResponse.java
package com.olive.dto;

import java.util.List;
import java.util.Collections;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private List<Long> projectIds;
    private List<String> projectNames;

    public AuthResponse(String accessToken, Long id, String email, String fullName, String role, List<Long> projectIds, List<String> projectNames) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.projectIds = (projectIds != null) ? projectIds : Collections.emptyList();
        this.projectNames = (projectNames != null) ? projectNames : Collections.emptyList();
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = (projectIds != null) ? projectIds : Collections.emptyList();
    }

    public List<String> getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(List<String> projectNames) {
        this.projectNames = (projectNames != null) ? projectNames : Collections.emptyList();
    }
}
