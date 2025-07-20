package com.olive.dto;

public class TaskLogDetailDTO {
    private String taskName;
    private double hours;
    private String comments;

    public TaskLogDetailDTO(String taskName, double hours, String comments) {
        this.taskName = taskName;
        this.hours = hours;
        this.comments = comments;
    }

    // Getters
    public String getTaskName() { return taskName; }
    public double getHours() { return hours; }
    public String getComments() { return comments; }
}