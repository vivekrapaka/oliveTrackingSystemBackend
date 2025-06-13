package com.olive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeammateCreateRequest {
    @NotBlank(message = "Teammate name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 50, message = "Role cannot exceed 50 characters")
    private String role; // New field

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone; // New field

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department; // New field

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location; // New field

    // Getters and Setters
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
}
