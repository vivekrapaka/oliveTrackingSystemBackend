package com.olive.dto;

import java.util.List;

public class TaskTimeSummaryResponse {
    private Long taskId;
    private String taskName;
    private double totalHours;
    private String devManagerName;
    private String testManagerName;
    private Double developmentDueHours;
    private Double testingDueHours;
    private List<TeammateEffortDTO> developerEffort;
    private List<TeammateEffortDTO> testerEffort;

    public TaskTimeSummaryResponse(Long taskId, String taskName, double totalHours, String devManagerName, String testManagerName, Double developmentDueHours, Double testingDueHours, List<TeammateEffortDTO> developerEffort, List<TeammateEffortDTO> testerEffort) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.totalHours = totalHours;
        this.devManagerName = devManagerName;
        this.testManagerName = testManagerName;
        this.developmentDueHours = developmentDueHours;
        this.testingDueHours = testingDueHours;
        this.developerEffort = developerEffort;
        this.testerEffort = testerEffort;
    }

    // Getters
    public Long getTaskId() { return taskId; }
    public String getTaskName() { return taskName; }
    public double getTotalHours() { return totalHours; }
    public String getDevManagerName() { return devManagerName; }
    public String getTestManagerName() { return testManagerName; }
    public Double getDevelopmentDueHours() { return developmentDueHours; }
    public Double getTestingDueHours() { return testingDueHours; }
    public List<TeammateEffortDTO> getDeveloperEffort() { return developerEffort; }
    public List<TeammateEffortDTO> getTesterEffort() { return testerEffort; }
}