package com.olive.dto;

import java.util.List;

public class DashboardTeammateDTO {
    private Long id;
    private String name;
    private String role;
    private String email;
    private String phone;
    private String department;
    private String location;
    private long tasksAssigned;
    private Long projectId; // NEW: projectId
    private String projectName; // NEW: projectName

    public DashboardTeammateDTO(Long id, String name, String role, String email, String phone, String department, String location, long tasksAssigned, Long projectId, String projectName) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.location = location;
        this.tasksAssigned = tasksAssigned;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public DashboardTeammateDTO(Long teammateId, String name, String role, String email, String phone, String department, String location, long tasksAssignedToTeammate, List<Long> teammateProjectIdsList, List<String> teammateProjectNames) {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTasksAssigned() {
        return tasksAssigned;
    }

    public void setTasksAssigned(long tasksAssigned) {
        this.tasksAssigned = tasksAssigned;
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
