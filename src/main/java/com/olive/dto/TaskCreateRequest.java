package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class TaskCreateRequest {
    @NotBlank(message = "Task name is required")
    @Size(max = 255, message = "Task name cannot exceed 255 characters")
    private String taskName;

    private String description;

    @NotBlank(message = "Current stage is required")
    private String currentStageName; // Use stage name from frontend

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private String issueType; // e.g., 'BRD', 'Production Issue', 'Enhancement', 'Bug'

    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Received date cannot be in the future")
    private LocalDate receivedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate developmentStartDate;

    private List<Long> assignedTeammateIds; // IDs of teammates to assign

    // Getters and Setters
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

    public String getCurrentStageName() {
        return currentStageName;
    }

    public void setCurrentStageName(String currentStageName) {
        this.currentStageName = currentStageName;
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

    public List<Long> getAssignedTeammateIds() {
        return assignedTeammateIds;
    }

    public void setAssignedTeammateIds(List<Long> assignedTeammateIds) {
        this.assignedTeammateIds = assignedTeammateIds;
    }
}
