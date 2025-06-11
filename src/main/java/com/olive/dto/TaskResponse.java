package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class TaskResponse {
    private Long taskId;
    private String taskName;
    private String description;
    private String currentStageName;
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
    private List<TeammateResponse> assignedTeammates; // List of DTOs for assigned teammates

    public TaskResponse(Long taskId, String taskName, String description, String currentStageName, LocalDate startDate, LocalDate dueDate, Boolean isCompleted, String issueType, LocalDate receivedDate, LocalDate developmentStartDate, Boolean isCodeReviewDone, Boolean isCmcDone, List<TeammateResponse> assignedTeammates) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.currentStageName = currentStageName;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.issueType = issueType;
        this.receivedDate = receivedDate;
        this.developmentStartDate = developmentStartDate;
        this.isCodeReviewDone = isCodeReviewDone;
        this.isCmcDone = isCmcDone;
        this.assignedTeammates = assignedTeammates;
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

    public List<TeammateResponse> getAssignedTeammates() {
        return assignedTeammates;
    }

    public void setAssignedTeammates(List<TeammateResponse> assignedTeammates) {
        this.assignedTeammates = assignedTeammates;
    }
}
