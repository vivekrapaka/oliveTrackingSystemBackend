package com.olive.dto;

import java.util.List;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String roleTitle;
    private String functionalGroup;
    private List<Long> projectIds;
    private List<String> projectNames;
    private String phone;
    private String location;

    public UserResponse(Long id, String fullName, String email, String roleTitle, String functionalGroup, List<Long> projectIds, List<String> projectNames, String phone, String location) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.roleTitle = roleTitle;
        this.functionalGroup = functionalGroup;
        this.projectIds = projectIds;
        this.projectNames = projectNames;
        this.phone = phone;
        this.location = location;
    }

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRoleTitle() { return roleTitle; }
    public String getFunctionalGroup() { return functionalGroup; }
    public List<Long> getProjectIds() { return projectIds; }
    public List<String> getProjectNames() { return projectNames; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
}
