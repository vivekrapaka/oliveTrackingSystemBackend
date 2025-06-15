package com.olive.dto;

public class DashboardTeammateDTO {
    private Long id;
    private String name;
    private String role;
    private String email;
    private String phone; // New field
    private String department; // New field
    private String location; // New field
    private long tasksAssigned; // Count of active tasks assigned

    public DashboardTeammateDTO(Long id, String name, String role, String email, String phone, String department, String location, long tasksAssigned) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.location = location;
        this.tasksAssigned = tasksAssigned;
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
}
