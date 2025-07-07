// backend/src/main/java/com/olive/dto/TeammateCreateRequest.java
package com.olive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class TeammateCreateRequest {

    @NotBlank(message = "Full name cannot be empty")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName; // Consistent with User entity

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Role cannot be empty")
    private String role;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank(message = "Department cannot be empty")
    private String department;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    private String avatar;

    private List<Long> projectIds; // List of project IDs to assign to this teammate

    // Constructors
    public TeammateCreateRequest() {
    }

    public TeammateCreateRequest(String fullName, String email, String role, String phone, String department, String location, String avatar, List<Long> projectIds) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.department = department;
        this.location = location;
        this.avatar = avatar;
        this.projectIds = projectIds;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public String getLocation() {
        return location;
    }

    public String getAvatar() {
        return avatar;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }
}
