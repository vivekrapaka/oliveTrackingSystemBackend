package com.olive.model;

import com.olive.model.enums.TaskStatus;
import com.olive.model.enums.TaskType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String taskName;

    // CRITICAL CHANGE: Changed from Long to String to support "1.1" style sub-task numbering
    @Column(nullable = false, unique = true)
    private String sequenceNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Task> subTasks = new HashSet<>();

    private LocalDate receivedDate;
    private LocalDate developmentStartDate;
    private LocalDate dueDate;

    @Column(nullable = false)
    private String priority;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_teammate_assignments",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "teammate_id")
    )
    private Set<Teammate> assignedTeammates = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String documentPath;
    private String commitId;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TaskActivity> activities = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WorkLog> workLogs = new HashSet<>();

    public Task() {}

    // Getters and Setters
    public Long getTaskId() { return id; }
    public void setTaskId(Long id) { this.id = id; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(String sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Task getParentTask() { return parentTask; }
    public void setParentTask(Task parentTask) { this.parentTask = parentTask; }
    public Set<Task> getSubTasks() { return subTasks; }
    public void setSubTasks(Set<Task> subTasks) { this.subTasks = subTasks != null ? subTasks : new HashSet<>(); }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public LocalDate getDevelopmentStartDate() { return developmentStartDate; }
    public void setDevelopmentStartDate(LocalDate developmentStartDate) { this.developmentStartDate = developmentStartDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Set<Teammate> getAssignedTeammates() { return assignedTeammates; }
    public void setAssignedTeammates(Set<Teammate> assignedTeammates) { this.assignedTeammates = assignedTeammates != null ? assignedTeammates : new HashSet<>(); }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public Set<TaskActivity> getActivities() { return activities; }
    public void setActivities(Set<TaskActivity> activities) { this.activities = activities; }
    public Set<WorkLog> getWorkLogs() { return workLogs; }
    public void setWorkLogs(Set<WorkLog> workLogs) { this.workLogs = workLogs; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Task task = (Task) o; return Objects.equals(id, task.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @PrePersist @PreUpdate
    public void convertTaskNameToUpper() { if (this.taskName != null) { this.taskName = this.taskName.toUpperCase(); } }
}
