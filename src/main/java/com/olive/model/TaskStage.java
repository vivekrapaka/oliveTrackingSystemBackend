package com.olive.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "task_stages")
public class TaskStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stageId;

    @Column(nullable = false, unique = true, length = 50)
    private String stageName; // e.g., SIT, DEV, Pre-Prod, Prod

    // --- Constructors ---
    public TaskStage() {
    }

    public TaskStage(String stageName) {
        this.stageName = stageName;
    }

    // --- Getters and Setters ---
    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(Integer stageId) {
        this.stageId = stageId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskStage taskStage = (TaskStage) o;
        return Objects.equals(stageId, taskStage.stageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stageId);
    }
}
