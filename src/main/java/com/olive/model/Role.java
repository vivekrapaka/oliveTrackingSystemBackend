package com.olive.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title; // e.g., "SDEII", "SDM1"

    @Column(nullable = false)
    private String functionalGroup; // e.g., "DEVELOPER", "MANAGER", "TESTER"

    public Role() {}

    public Role(String title, String functionalGroup) {
        this.title = title;
        this.functionalGroup = functionalGroup;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFunctionalGroup() { return functionalGroup; }
    public void setFunctionalGroup(String functionalGroup) { this.functionalGroup = functionalGroup; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
