package com.olive.model;

import com.olive.dto.TeammateResponse;
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

    // AvailabilityStatus: "Free" or "Occupied"
    @Column(nullable = false, length = 20)
    private String availabilityStatus = "Free"; // Default status

    // --- Constructors ---
    public Teammate() {
    }

    public Teammate(String name, String email) {
        this.name = name;
        this.email = email;
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
//TODO
    public TeammateResponse convertToDto() {

        return null;
    }
}
