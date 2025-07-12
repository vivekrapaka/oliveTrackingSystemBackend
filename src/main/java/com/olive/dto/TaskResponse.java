package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.olive.model.enums.TaskStatus;
import com.olive.model.enums.TaskType;

import java.time.LocalDate;
import java.util.List;

public class TaskResponse {
    private Long id;
    private String name;
    private String taskNumber;
    private String description;
    private TaskType taskType;
    private TaskStatus status;
    private Long parentId;
    private String parentTaskTitle;
    private String parentTaskFormattedNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate developmentStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private String priority;
    private List<Long> assignedTeammateIds;
    private List<String> assignedTeammateNames;
    private Long projectId;
    private String projectName;
    private String documentPath;
    private String commitId;

    private String developerName; // NEW
    private String testerName;

    public TaskResponse(Long id, String name, String taskNumber, String description, TaskType taskType, TaskStatus status,
                        Long parentId, String parentTaskTitle, String parentTaskFormattedNumber,
                        LocalDate receivedDate, LocalDate developmentStartDate, LocalDate dueDate, String priority,
                        List<Long> assignedTeammateIds, List<String> assignedTeammateNames,String developerName,String testerName,
                        Long projectId, String projectName, String documentPath, String commitId) {
        this.id = id;
        this.name = name;
        this.taskNumber = taskNumber;
        this.description = description;
        this.taskType = taskType;
        this.status = status;
        this.parentId = parentId;
        this.parentTaskTitle = parentTaskTitle;
        this.parentTaskFormattedNumber = parentTaskFormattedNumber;
        this.receivedDate = receivedDate;
        this.developmentStartDate = developmentStartDate;
        this.dueDate = dueDate;
        this.priority = priority;
        this.assignedTeammateIds = assignedTeammateIds;
        this.assignedTeammateNames = assignedTeammateNames;
        this.projectId = projectId;
        this.projectName = projectName;
        this.documentPath = documentPath;
        this.commitId = commitId;
        this.developerName = developerName;
        this.testerName = testerName;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getTaskNumber() { return taskNumber; }
    public String getDescription() { return description; }
    public TaskType getTaskType() { return taskType; }
    public TaskStatus getStatus() { return status; }
    public Long getParentId() { return parentId; }
    public String getParentTaskTitle() { return parentTaskTitle; }
    public String getParentTaskFormattedNumber() { return parentTaskFormattedNumber; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public LocalDate getDevelopmentStartDate() { return developmentStartDate; }
    public LocalDate getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public List<Long> getAssignedTeammateIds() { return assignedTeammateIds; }
    public List<String> getAssignedTeammateNames() { return assignedTeammateNames; }
    public Long getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getDocumentPath() { return documentPath; }
    public String getCommitId() { return commitId; }
    public String getDeveloperName() { return developerName; }
    public String getTesterName() { return testerName; }
}