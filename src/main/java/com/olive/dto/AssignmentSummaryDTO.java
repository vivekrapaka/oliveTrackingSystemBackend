package com.olive.dto;

public class AssignmentSummaryDTO {
    private Long taskId;
    private String taskName;
    private Long teammateId;
    private String teammateName;
    private String currentStageName;

    public AssignmentSummaryDTO(Long taskId, String taskName, Long teammateId, String teammateName, String currentStageName) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.teammateId = teammateId;
        this.teammateName = teammateName;
        this.currentStageName = currentStageName;
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

    public Long getTeammateId() {
        return teammateId;
    }

    public void setTeammateId(Long teammateId) {
        this.teammateId = teammateId;
    }

    public String getTeammateName() {
        return teammateName;
    }

    public void setTeammateName(String teammateName) {
        this.teammateName = teammateName;
    }

    public String getCurrentStageName() {
        return currentStageName;
    }

    public void setCurrentStageName(String currentStageName) {
        this.currentStageName = currentStageName;
    }
}
