package com.olive.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255) // Store hashed password
    private String password;

    // NEW: Role field
    // Using String to allow flexibility for now, but enum is also a good option.
    @Column(nullable = false, length = 50)
    private String role; // e.g., "ADMIN", "MANAGER", "BA", "TEAM_MEMBER"

    // NEW: Team ID field
    // This will be null if the user is an ADMIN or currently unassigned.
    @Column(name = "team_id")
    private Long teamId; // Foreign Key to Team entity (if Team entity exists, otherwise just an ID)


    // Default constructor
    public User() {
    }

    // Updated constructor to include role and teamId
    public User(String fullName, String email, String password, String role, Long teamId) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.teamId = teamId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);

    }
}
