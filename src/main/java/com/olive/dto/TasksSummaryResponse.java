package com.olive.dto;

import java.util.List;

public class TasksSummaryResponse {
    private long totalTasksCount;
    private List<TaskResponse> tasks;

    public TasksSummaryResponse(long totalTasksCount, List<TaskResponse> tasks) {
        this.totalTasksCount = totalTasksCount;
        this.tasks = tasks;
    }

    // Getters and Setters
    public long getTotalTasksCount() {
        return totalTasksCount;
    }

    public void setTotalTasksCount(long totalTasksCount) {
        this.totalTasksCount = totalTasksCount;
    }

    public List<TaskResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskResponse> tasks) {
        this.tasks = tasks;
    }
}
