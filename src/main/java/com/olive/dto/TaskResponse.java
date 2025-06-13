package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class TaskResponse { private Long id; // Renamed from taskId
    private String name; // Renamed from taskName
    private String issueType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate developmentStartDate;
    private String currentStage;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private List<String> assignedTeammates; // Renamed from assignedTeammateNames
    private String priority;
    private Boolean isCompleted;
    private Boolean iscmcDone; // Renamed from isCmcDone


    // Updated constructor
    public TaskResponse(Long id, String name, String issueType, LocalDate receivedDate,
                        LocalDate developmentStartDate, String currentStage, LocalDate dueDate,
                        List<String> assignedTeammates, String priority, Boolean isCompleted,
                        Boolean iscmcDone) {
        this.id = id;
        this.name = name;
        this.issueType = issueType;
        this.receivedDate = receivedDate;
        this.developmentStartDate = developmentStartDate;
        this.currentStage = currentStage;
        this.dueDate = dueDate;
        this.assignedTeammates = assignedTeammates;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.iscmcDone = iscmcDone;
    }

    // Getters and Setters - Renamed and removed as per the request
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

    public Boolean getIscmcDone() { // Renamed getter
        return iscmcDone;
    }

    public void setIscmcDone(Boolean iscmcDone) { // Renamed setter
        this.iscmcDone = iscmcDone;
    }
}
