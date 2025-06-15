package com.olive.dto;

public class TeammateResponse {
    private Long id; // Renamed from teammateId
    private String name;
    private String email;
    private String role; // New field
    private String phone; // New field
    private String department; // New field
    private String location; // New field
    private String avatar; // New: Avatar field
    private String availabilityStatus; // This will now be derived by service
    private long tasksAssigned; // New: Number of active tasks assigned
    private long tasksCompleted; // New: Number of completed tasks assigned

    public TeammateResponse(Long id, String name, String email, String role, String phone, String department, String location, String avatar, String availabilityStatus, long tasksAssigned, long tasksCompleted) {
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
    }

    // Getters and Setters
    public Long getId() { // Renamed getter
        return id;
    }

    public void setId(Long id) { // Renamed setter
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAvatar() { // Getter for avatar
        return avatar;
    }

    public void setAvatar(String avatar) { // Setter for avatar
        this.avatar = avatar;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public long getTasksAssigned() {
        return tasksAssigned;
    }

    public void setTasksAssigned(long tasksAssigned) {
        this.tasksAssigned = tasksAssigned;
    }

    public long getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(long tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }
}
