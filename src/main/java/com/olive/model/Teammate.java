package com.olive.model;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "teammates")
public class Teammate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teammateId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 100) // Email for login/linking to User
    private String email;

    @Column(length = 50)
    private String role; // Role within the team/project (e.g., "Developer", "QA")

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String location;

    @Column(length = 255)
    private String avatar;

    @Column(nullable = false, length = 20)
    private String availabilityStatus = "Free"; // "Free" or "Occupied"

    // NEW: projectId to link teammate to a specific project
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    // --- Constructors ---
    public Teammate() {
    }

    // Updated constructor to include projectId
    public Teammate(String name, String email, String role, String phone, String department, String location, String avatar, Long projectId) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.department = department;
        this.location = location;
        this.avatar = avatar;
        this.projectId = projectId; // Initialize projectId
    }

    // --- Getters and Setters ---
    public Long getTeammateId() {
        return teammateId;
    }

    public void setTeammateId(Long teammateId) {
        this.teammateId = teammateId;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    // NEW: Getter and Setter for projectId
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teammate teammate = (Teammate) o;
        return Objects.equals(teammateId, teammate.teammateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teammateId);
    }

    @PrePersist
    @PreUpdate
    public void convertNameToUppercase() {
        if (this.name != null) {
            this.name = this.name.toUpperCase();
        }
    }
}
