package com.olive.dto;

import java.util.List;

public class TaskTimeSummaryResponse {
    private Long taskId;
    private String taskName;
    private double totalHours;
    private TimeLogBreakdownDTO breakdown;
    private String devManagerName;
    private String testManagerName;
    private List<String> developerNames; // NEW
    private List<String> testerNames;    // NEW

    public TaskTimeSummaryResponse(Long taskId, String taskName, double totalHours, TimeLogBreakdownDTO breakdown, String devManagerName, String testManagerName, List<String> developerNames, List<String> testerNames) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.totalHours = totalHours;
        this.breakdown = breakdown;
        this.devManagerName = devManagerName;
        this.testManagerName = testManagerName;
        this.developerNames = developerNames;
        this.testerNames = testerNames;
    }

    // Getters
    public Long getTaskId() { return taskId; }
    public String getTaskName() { return taskName; }
    public double getTotalHours() { return totalHours; }
    public TimeLogBreakdownDTO getBreakdown() { return breakdown; }
    public String getDevManagerName() { return devManagerName; }
    public String getTestManagerName() { return testManagerName; }
    public List<String> getDeveloperNames() { return developerNames; }
    public List<String> getTesterNames() { return testerNames; }
}