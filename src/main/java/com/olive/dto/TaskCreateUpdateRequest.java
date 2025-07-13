package com.olive.dto;

import com.olive.model.enums.TaskStatus;
import com.olive.model.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class TaskCreateUpdateRequest {

    @NotBlank
    @Size(max = 255)
    private String taskName;

    @Size(max = 2000)
    private String description;

    @NotNull
    private TaskType taskType;

    @NotNull
    private TaskStatus status;

    private Long parentId;
    private LocalDate receivedDate;
    private LocalDate developmentStartDate;

    private Double developmentDueHours;
    private Double testingDueHours;

    @NotBlank
    private String priority;

    private List<Long> developerIds;
    private List<Long> testerIds;

    @NotNull
    private Long projectId;

    private String documentPath;
    private String commitId;
    private String comment;

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
    public Double getDevelopmentDueHours() { return developmentDueHours; }
    public void setDevelopmentDueHours(Double developmentDueHours) { this.developmentDueHours = developmentDueHours; }
    public Double getTestingDueHours() { return testingDueHours; }
    public void setTestingDueHours(Double testingDueHours) { this.testingDueHours = testingDueHours; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public List<Long> getDeveloperIds() { return developerIds; }
    public void setDeveloperIds(List<Long> developerIds) { this.developerIds = developerIds; }
    public List<Long> getTesterIds() { return testerIds; }
    public void setTesterIds(List<Long> testerIds) { this.testerIds = testerIds; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
