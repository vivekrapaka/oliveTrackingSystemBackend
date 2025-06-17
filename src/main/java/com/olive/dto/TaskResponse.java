package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class TaskResponse {
    private Long id;
    private String name;
    private String taskNumber;
    private String description;
    private String issueType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate developmentStartDate;
    private String currentStage;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private List<String> assignedTeammates;
    private String priority;
    private Boolean isCompleted;
    private Boolean isCmcDone;
    private Long projectId; // NEW: projectId
    private String projectName; // NEW: projectName


    public TaskResponse(Long id, String name, String taskNumber, String description, String issueType, LocalDate receivedDate,
                        LocalDate developmentStartDate, String currentStage, LocalDate dueDate,
                        List<String> assignedTeammates, String priority, Boolean isCompleted,
                        Boolean isCmcDone, Long projectId, String projectName) {
        this.id = id;
        this.name = name;
        this.taskNumber = taskNumber;
        this.description = description;
        this.issueType = issueType;
        this.receivedDate = receivedDate;
        this.developmentStartDate = developmentStartDate;
        this.currentStage = currentStage;
        this.dueDate = dueDate;
        this.assignedTeammates = assignedTeammates;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.isCmcDone = isCmcDone;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<String> getAssignedTeammates() {
        return assignedTeammates;
    }

    public void setAssignedTeammates(List<String> assignedTeammates) {
        this.assignedTeammates = assignedTeammates;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public Boolean getIsCmcDone() {
        return isCmcDone;
    }

    public void setIsCmcDone(Boolean isCmcDone) {
        this.isCmcDone = isCmcDone;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
