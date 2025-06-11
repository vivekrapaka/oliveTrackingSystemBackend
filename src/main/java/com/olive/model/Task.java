package com.olive.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false)
    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER) // Eagerly load stage with task
    @JoinColumn(name = "current_stage_id", nullable = false)
    private TaskStage currentStage;

    private LocalDate startDate;
    private LocalDate dueDate;
    private Boolean isCompleted = false;

    // New Fields for Issue/BRD Tracking
    private String issueType; // e.g., 'BRD', 'Production Issue', 'Enhancement', 'Bug'
    private LocalDate receivedDate;
    private LocalDate developmentStartDate;

    // New Fields for Development Lifecycle Checkpoints
    private Boolean isCodeReviewDone = false;
    private Boolean isCmcDone = false;

    // --- Constructors ---
    public Task() {
    }

    public Task(String taskName, String description, TaskStage currentStage, LocalDate startDate, LocalDate dueDate,
                String issueType, LocalDate receivedDate, LocalDate developmentStartDate) {
        this.taskName = taskName;
        this.description = description;
        this.currentStage = currentStage;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.issueType = issueType;
        this.receivedDate = receivedDate;
        this.developmentStartDate = developmentStartDate;
    }

    // --- Getters and Setters ---
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(TaskStage currentStage) {
        this.currentStage = currentStage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDate getDevelopmentStartDate() {
        return developmentStartDate;
    }

    public void setDevelopmentStartDate(LocalDate developmentStartDate) {
        this.developmentStartDate = developmentStartDate;
    }

    public Boolean getIsCodeReviewDone() {
        return isCodeReviewDone;
    }

    public void setIsCodeReviewDone(Boolean codeReviewDone) {
        isCodeReviewDone = codeReviewDone;
    }

    public Boolean getIsCmcDone() {
        return isCmcDone;
    }

    public void setIsCmcDone(Boolean cmcDone) {
        isCmcDone = cmcDone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
}
