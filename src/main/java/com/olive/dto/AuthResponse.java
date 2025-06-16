package com.olive.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer"; // Default type for JWT
    private Long id;
    private String email;
    private String fullName; // To return full name after login

    // NEW: Role and Team ID fields
    private String role;
    private Long teamId;


    // Updated constructor to include role and teamId
    public AuthResponse(String token, Long id, String email, String fullName, String role, Long teamId) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.teamId = teamId;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // NEW Getters and Setters for role and teamId
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}