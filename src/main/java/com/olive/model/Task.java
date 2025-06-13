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

    // Task stage is now a String directly in the Task table
    @Column(nullable = false, length = 50)
    private String currentStage; // e.g., SIT, DEV, Pre-Prod, Prod

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

    // Assigned Teammates stored as a comma-separated string of names
    // This simplifies the schema as requested, but logic for parsing/joining will be in service.
    @Column(columnDefinition = "TEXT")
    private String assignedTeammateNames; // e.g., "John Doe,Jane Smith"

    @Column(length = 20) // New field for task priority
    private String priority; // e.g., "High", "Medium", "Low"

    // --- Constructors ---
    public Task() {
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

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
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

    public String getAssignedTeammateNames() {
        return assignedTeammateNames;
    }

    public void setAssignedTeammateNames(String assignedTeammateNames) {
        this.assignedTeammateNames = assignedTeammateNames;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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
