package com.olive.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "work_logs")
public class WorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teammate_id", nullable = false)
    private Teammate teammate;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private Double hoursSpent;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    public WorkLog() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public Teammate getTeammate() { return teammate; }
    public void setTeammate(Teammate teammate) { this.teammate = teammate; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public Double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(Double hoursSpent) { this.hoursSpent = hoursSpent; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; WorkLog workLog = (WorkLog) o; return Objects.equals(id, workLog.id); }

    @Override
    public int hashCode() { return Objects.hash(id); }
}