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

    @Column(nullable = false, unique = true)
    private String sequenceNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)")
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

    private Double developmentDueHours;
    private Double testingDueHours;

    @Column(nullable = false)
    private String priority;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_developer_assignments",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "teammate_id")
    )
    private Set<Teammate> assignedDevelopers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_tester_assignments",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "teammate_id")
    )
    private Set<Teammate> assignedTesters = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String documentPath;
    private String commitId;
    private String bitbucketBranch;
    private String sharepointUrl;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TaskActivity> activities = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WorkLog> workLogs = new HashSet<>();

    public Task() {}

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
    public void setSubTasks(Set<Task> subTasks) { this.subTasks = subTasks; }
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
    public Set<Teammate> getAssignedDevelopers() { return assignedDevelopers; }
    public void setAssignedDevelopers(Set<Teammate> assignedDevelopers) { this.assignedDevelopers = assignedDevelopers; }
    public Set<Teammate> getAssignedTesters() { return assignedTesters; }
    public void setAssignedTesters(Set<Teammate> assignedTesters) { this.assignedTesters = assignedTesters; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getBitbucketBranch() { return bitbucketBranch; }
    public void setBitbucketBranch(String bitbucketBranch) { this.bitbucketBranch = bitbucketBranch; }
    public String getSharepointUrl() { return sharepointUrl; }
    public void setSharepointUrl(String sharepointUrl) { this.sharepointUrl = sharepointUrl; }
    public Set<TaskActivity> getActivities() { return activities; }
    public void setActivities(Set<TaskActivity> activities) { this.activities = activities; }
    public Set<WorkLog> getWorkLogs() { return workLogs; }
    public void setWorkLogs(Set<WorkLog> workLogs) { this.workLogs = workLogs; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Task task = (Task) o; return Objects.equals(id, task.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
