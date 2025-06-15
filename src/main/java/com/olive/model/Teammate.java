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

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 50) // New field for role
    private String role;

    @Column(length = 20) // New field for phone
    private String phone;

    @Column(length = 100) // New field for department
    private String department;

    @Column(length = 100) // New field for location
    private String location;

    @Column(length = 255) // New: Avatar field
    private String avatar;

    // AvailabilityStatus: "Free" or "Occupied"
    // This status will be derived and updated by TaskService based on active assignments.
    @Column(nullable = false, length = 20)
    private String availabilityStatus = "Free"; // Default status

    // --- Constructors ---
    public Teammate() {
    }

    public Teammate(String name, String email, String role, String phone, String department, String location, String avatar) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.department = department;
        this.location = location;
        this.avatar = avatar;
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

    /**
     * JPA lifecycle callback to convert the name to uppercase before persisting or updating.
     */
    @PrePersist
    @PreUpdate
    public void convertNameToUppercase() {
        if (this.name != null) {
            this.name = this.name.toUpperCase();
        }
    }
}
