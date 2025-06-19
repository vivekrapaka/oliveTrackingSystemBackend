package com.olive.model;

import jakarta.persistence.*;

import java.util.List;
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

    @Column(nullable = false, length = 50)
    private String role; // e.g., "ADMIN", "HR", "MANAGER", "BA", "TEAMLEAD", "TEAMMEMBER"

    // UPDATED: projectIds to handle multiple project assignments for MANAGER/BA
    // Stored as a comma-separated string in DB, converted to List<Long> in entity
    @Column(name = "project_ids")
    @Convert(converter = JpaLongListConverter.class) // NEW: Apply the converter
    private List<Long> projectIds;

    // Default constructor
    public User() {
    }

    // Updated constructor to use List<Long> for projectIds
    public User(String fullName, String email, String password, String role, List<Long> projectIds) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.projectIds = projectIds;
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
