package com.olive.dto;

import java.util.List;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private List<Long> projectIds;
    private List<String> projectNames;
    private String phone;     // NEW: Added phone field
    private String location;  // NEW: Added location field

    public UserResponse(Long id, String fullName, String email, String role, List<Long> projectIds, List<String> projectNames, String phone, String location) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.projectIds = projectIds;
        this.projectNames = projectNames;
        this.phone = phone;         // NEW: Updated constructor
        this.location = location;   // NEW: Updated constructor
    }

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public List<Long> getProjectIds() { return projectIds; }
    public List<String> getProjectNames() { return projectNames; }
    public String getPhone() { return phone; }         // NEW: Getter for phone
    public String getLocation() { return location; }   // NEW: Getter for location
}
