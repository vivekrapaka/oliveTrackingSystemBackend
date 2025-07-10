package com.olive.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "teammates")
public class Teammate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String phone;
    private String department;
    private String location;
    private String avatar;

    @Column(nullable = false)
    private String availabilityStatus = "Free";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "teammate_projects",
            joinColumns = @JoinColumn(name = "teammate_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();

    @ManyToMany(mappedBy = "assignedTeammates", fetch = FetchType.LAZY)
    private Set<Task> assignedTasks = new HashSet<>();

    public Teammate() {}

    // Getters and Setters
    public Long getTeammateId() { return id; }
    public void setTeammateId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public Set<Project> getProjects() { return projects; }
    public void setProjects(Set<Project> projects) { this.projects = projects != null ? projects : new HashSet<>(); }
    public Set<Task> getAssignedTasks() { return assignedTasks; }
    public void setAssignedTasks(Set<Task> assignedTasks) { this.assignedTasks = assignedTasks != null ? assignedTasks : new HashSet<>(); }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Teammate teammate = (Teammate) o; return Objects.equals(id, teammate.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }
}