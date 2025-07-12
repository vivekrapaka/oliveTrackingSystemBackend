package com.olive.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private Set<Teammate> teammates = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    public Project() {}

    public Project(String projectName, String description, LocalDate startDate, LocalDate endDate) {
        this.projectName = projectName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getProjectId() { return id; }
    public void setProjectId(Long id) { this.id = id; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Set<Teammate> getTeammates() { return teammates; }
    public void setTeammates(Set<Teammate> teammates) { this.teammates = teammates != null ? teammates : new HashSet<>(); }
    public Set<Task> getTasks() { return tasks; }
    public void setTasks(Set<Task> tasks) { this.tasks = tasks != null ? tasks : new HashSet<>(); }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Project project = (Project) o; return Objects.equals(id, project.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @PrePersist @PreUpdate
    public void convertProjectNameToUppercase() { if (this.projectName != null) { this.projectName = this.projectName.toUpperCase(); } }
}