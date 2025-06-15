package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class DashboardTaskDTO {
    private Long id;
    private String name;
    private String stage;
    private String assignee; // Combined from assignedTeammateNames
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private String priority;
    private String taskNumber; // NEW: field for sequence number

    // Updated constructor to include taskNumber
    public DashboardTaskDTO(Long id, String name, String stage, String assignee, LocalDate dueDate, String priority, String taskNumber) {
        this.id = id;
        this.name = name;
        this.stage = stage;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.priority = priority;
        this.taskNumber = taskNumber;
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

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTaskNumber() { // Getter for taskNumber
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) { // Setter for taskNumber
        this.taskNumber = taskNumber;
    }
}
