package com.olive.dto;

public class TaskLogDetailDTO {
    private String taskName;
    private double hours;

    public TaskLogDetailDTO(String taskName, double hours) {
        this.taskName = taskName;
        this.hours = hours;
    }

    public String getTaskName() {
        return taskName;
    }

    public double getHours() {
        return hours;
    }
}