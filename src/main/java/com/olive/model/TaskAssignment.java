package com.olive.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "task_assignments", uniqueConstraints = {@UniqueConstraint(columnNames = {"task_id", "teammate_id"})})
public class TaskAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teammate_id", nullable = false)
    private Teammate teammate;

    private LocalDate assignedDate = LocalDate.now(); // Default to current date

    private Boolean isActive = true; // Set to false if assignment ends or teammate is no longer working on it

    // --- Constructors ---
    public TaskAssignment() {
    }

    public TaskAssignment(Task task, Teammate teammate) {
        this.task = task;
        this.teammate = teammate;
    }

    // --- Getters and Setters ---
    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Teammate getTeammate() {
        return teammate;
    }

    public void setTeammate(Teammate teammate) {
        this.teammate = teammate;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskAssignment that = (TaskAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }
}
