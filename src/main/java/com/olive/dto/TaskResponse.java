package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class TaskResponse {
    private Long taskId;
    private String taskName;
    private String description;
    private String currentStage; // Now a String
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private Boolean isCompleted;
    private String issueType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate developmentStartDate;
    private Boolean isCodeReviewDone;
    private Boolean isCmcDone;
    private List<String> assignedTeammateNames; // Now a list of names
    private String priority; // New field

    public TaskResponse(Long taskId, String taskName, String description, String currentStage, LocalDate startDate, LocalDate dueDate, Boolean isCompleted, String issueType, LocalDate receivedDate, LocalDate developmentStartDate, Boolean isCodeReviewDone, Boolean isCmcDone, List<String> assignedTeammateNames, String priority) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.currentStage = currentStage;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.issueType = issueType;
        this.receivedDate = receivedDate;
        this.developmentStartDate = developmentStartDate;
        this.isCodeReviewDone = isCodeReviewDone;
        this.isCmcDone = isCmcDone;
        this.assignedTeammateNames = assignedTeammateNames;
        this.priority = priority;
    }

    // Getters and Setters
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

    public List<String> getAssignedTeammateNames() {
        return assignedTeammateNames;
    }

    public void setAssignedTeammateNames(List<String> assignedTeammateNames) {
        this.assignedTeammateNames = assignedTeammateNames;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
