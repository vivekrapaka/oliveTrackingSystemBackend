package com.olive.dto;

import com.olive.model.enums.TaskStatus;
import com.olive.model.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class TaskCreateUpdateRequest {

    @NotBlank(message = "Task name cannot be empty")
    @Size(max = 255, message = "Task name cannot exceed 255 characters")
    private String taskName;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Task Type cannot be empty")
    private TaskType taskType;

    @NotNull(message = "Status cannot be empty")
    private TaskStatus status;

    private Long parentId;

    private LocalDate receivedDate;
    private LocalDate developmentStartDate;
    private LocalDate dueDate;

    @NotBlank(message = "Priority cannot be empty")
    private String priority;

    private List<Long> assignedTeammateIds;

    @NotNull(message = "Project ID cannot be empty")
    private Long projectId;

    private String documentPath;
    private String commitId;

    // NEW: Field for mandatory comments during workflow transitions
    private String comment;

    // Getters and Setters
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public LocalDate getDevelopmentStartDate() { return developmentStartDate; }
    public void setDevelopmentStartDate(LocalDate developmentStartDate) { this.developmentStartDate = developmentStartDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public List<Long> getAssignedTeammateIds() { return assignedTeammateIds; }
    public void setAssignedTeammateIds(List<Long> assignedTeammateIds) { this.assignedTeammateIds = assignedTeammateIds; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}