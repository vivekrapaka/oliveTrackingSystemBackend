package com.olive.dto;

public class TaskTimeSummaryResponse {
    private Long taskId;
    private String taskName;
    private double totalHours;
    private TimeLogBreakdownDTO breakdown;

    public TaskTimeSummaryResponse(Long taskId, String taskName, double totalHours, TimeLogBreakdownDTO breakdown) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.totalHours = totalHours;
        this.breakdown = breakdown;
    }

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

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public TimeLogBreakdownDTO getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(TimeLogBreakdownDTO breakdown) {
        this.breakdown = breakdown;
    }
}
