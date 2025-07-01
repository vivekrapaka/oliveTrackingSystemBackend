package com.olive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    @NotBlank(message = "Phone number is required") // NEW
    @Size(max = 20, message = "Phone number cannot exceed 20 characters") // NEW
    private String phone;

    @NotBlank(message = "Location is required") // NEW
    @Size(max = 100, message = "Location cannot exceed 100 characters") // NEW
    private String location;

    // Getters and Setters
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

    public String getPhone() { // NEW
        return phone;
    }

    public void setPhone(String phone) { // NEW
        this.phone = phone;
    }

    public String getLocation() { // NEW
        return location;
    }

    public void setLocation(String location) { // NEW
        this.location = location;
    }
}
