package com.olive.repository;

import com.olive.model.Task;
import com.olive.model.TaskStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
    // Custom queries can be added here, e.g., find tasks by stage
    List<Task> findByCurrentStage(TaskStage currentStage);
    List<Task> findByIsCompleted(Boolean isCompleted);
    List<Task> findByIssueType(String issueType);
    List<Task> findByIsCodeReviewDone(Boolean isCodeReviewDone);
    List<Task> findByIsCmcDone(Boolean isCmcDone);
}
