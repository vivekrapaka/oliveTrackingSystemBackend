package com.olive.dto;

import java.util.List;

public class TeammateResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String department;
    private String location;
    private String avatar;
    private String availabilityStatus;
    private long tasksAssigned;
    private long tasksCompleted;
    private List<Long> projectIds;
    private List<String> projectNames;

    public TeammateResponse(Long id, String name, String email, String role, String phone, String department, String location, String avatar, String availabilityStatus, long tasksAssigned, long tasksCompleted, List<Long> projectIds, List<String> projectNames) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.department = department;
        this.location = location;
        this.avatar = avatar;
        this.availabilityStatus = availabilityStatus;
        this.tasksAssigned = tasksAssigned;
        this.tasksCompleted = tasksCompleted;
        this.projectIds = projectIds;
        this.projectNames = projectNames;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public String getLocation() { return location; }
    public String getAvatar() { return avatar; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public long getTasksAssigned() { return tasksAssigned; }
    public long getTasksCompleted() { return tasksCompleted; }
    public List<Long> getProjectIds() { return projectIds; }
    public List<String> getProjectNames() { return projectNames; }
}
