package com.olive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UserCreateUpdateRequest {
    private Long id; // Added for updates
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    // Password is only required for create, optional for update.
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|HR|MANAGER|TEAMLEAD|BA|TEAMMEMBER", message = "Invalid role. Must be ADMIN, HR, MANAGER, TEAMLEAD, BA, or TEAMMEMBER.")
    private String role;

    // UPDATED: projectIds to handle multiple project assignments for MANAGER/BA
    // For ADMIN/HR: null/empty list. For TEAMLEAD/TEAMMEMBER: list with exactly one ID. For MANAGER/BA: list with one or more IDs.
    private List<Long> projectIds;

    // Getters and Setters
    public Long getId() { // NEW
        return id;
    }

    public void setId(Long id) { // NEW
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // UPDATED: Getter and Setter for projectIds (List<Long>)
    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }
}
