package com.olive.model;

import com.olive.converter.JpaLongListConverter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role;

    @Convert(converter = JpaLongListConverter.class)
    @Column(name = "project_ids_list")
    private List<Long> projectIds = new ArrayList<>();

    public User() {
    }

    public User(String fullName, String email, String password, String role, List<Long> projectIds) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.projectIds = projectIds != null ? new ArrayList<>(projectIds) : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<Long> getProjectIds() { return projectIds; }
    public void setProjectIds(List<Long> projectIds) { this.projectIds = projectIds != null ? new ArrayList<>(projectIds) : new ArrayList<>();}

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; User user = (User) o; return Objects.equals(id, user.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @PrePersist @PreUpdate
    public void convertEmailToLowerCase() { if (this.email != null) { this.email = this.email.toLowerCase(); } }
}
