package com.olive.dto;

import java.util.List;
import java.util.Collections;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private String roleTitle;
    private String functionalGroup; // FIX: Added functionalGroup
    private List<Long> projectIds;
    private List<String> projectNames;

    public AuthResponse(String accessToken, Long id, String email, String fullName, String roleTitle, String functionalGroup, List<Long> projectIds, List<String> projectNames) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.roleTitle = roleTitle;
        this.functionalGroup = functionalGroup; // FIX: Updated constructor
        this.projectIds = (projectIds != null) ? projectIds : Collections.emptyList();
        this.projectNames = (projectNames != null) ? projectNames : Collections.emptyList();
    }

    // Getters
    public String getToken() { return token; }
    public String getType() { return type; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRoleTitle() { return roleTitle; }
    public String getFunctionalGroup() { return functionalGroup; } // FIX: Added getter
    public List<Long> getProjectIds() { return projectIds; }
    public List<String> getProjectNames() { return projectNames; }
}