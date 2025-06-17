package com.olive.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String projectName; // e.g., "Kotak", "Union Bank"

    @Column(columnDefinition = "TEXT")
    private String description; // Optional project description

    // Constructors
    public Project() {
    }

    public Project(String projectName, String description) {
        this.projectName = projectName;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @PrePersist
    @PreUpdate
    public void convertProjectNameToUppercase() {
        if (this.projectName != null) {
            this.projectName = this.projectName.toUpperCase();
        }
    }
}
