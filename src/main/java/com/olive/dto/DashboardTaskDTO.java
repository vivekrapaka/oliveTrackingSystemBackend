package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class DashboardTaskDTO {
    private Long id;
    private String name;
    private String stage;
    private String priority;
    private String taskNumber;
    private Long projectId;
    private String projectName;
    private String developerName;
    private String testerName;
    private Double developmentDueHours; // NEW
    private Double testingDueHours;     // NEW

    public DashboardTaskDTO(Long id, String name, String stage, String priority, String taskNumber, Long projectId, String projectName, String developerName, String testerName, Double developmentDueHours, Double testingDueHours) {
        this.id = id;
        this.name = name;
        this.stage = stage;
        this.priority = priority;
        this.taskNumber = taskNumber;
        this.projectId = projectId;
        this.projectName = projectName;
        this.developerName = developerName;
        this.testerName = testerName;
        this.developmentDueHours = developmentDueHours;
        this.testingDueHours = testingDueHours;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getTaskNumber() { return taskNumber; }
    public void setTaskNumber(String taskNumber) { this.taskNumber = taskNumber; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getDeveloperName() { return developerName; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }
    public String getTesterName() { return testerName; }
    public void setTesterName(String testerName) { this.testerName = testerName; }
    public Double getDevelopmentDueHours() { return developmentDueHours; }
    public void setDevelopmentDueHours(Double developmentDueHours) { this.developmentDueHours = developmentDueHours; }
    public Double getTestingDueHours() { return testingDueHours; }
    public void setTestingDueHours(Double testingDueHours) { this.testingDueHours = testingDueHours; }
}
