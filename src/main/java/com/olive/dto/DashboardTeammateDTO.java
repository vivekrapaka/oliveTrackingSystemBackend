package com.olive.dto;

import java.util.List;

public class DashboardTeammateDTO {
    private Long id;
    private String name;
    private String roleTitle; // Changed from role
    private String email;
    private String phone;
    private String department;
    private String location;
    private long tasksAssigned;
    private List<Long> projectIds;
    private List<String> projectNames;

    public DashboardTeammateDTO(Long id, String name, String roleTitle, String email, String phone, String department, String location, long tasksAssigned, List<Long> projectIds, List<String> projectNames) {
        this.id = id;
        this.name = name;
        this.roleTitle = roleTitle;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.location = location;
        this.tasksAssigned = tasksAssigned;
        this.projectIds = projectIds;
        this.projectNames = projectNames;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public long getTasksAssigned() { return tasksAssigned; }
    public void setTasksAssigned(long tasksAssigned) { this.tasksAssigned = tasksAssigned; }
    public List<Long> getProjectIds() { return projectIds; }
    public void setProjectIds(List<Long> projectIds) { this.projectIds = projectIds; }
    public List<String> getProjectNames() { return projectNames; }
    public void setProjectNames(List<String> projectNames) { this.projectNames = projectNames; }
}
