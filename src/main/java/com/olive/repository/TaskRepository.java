package com.olive.repository;

import com.olive.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByCurrentStage(String currentStage); // Find tasks by string stage name
    List<Task> findByIsCompleted(Boolean isCompleted);
    List<Task> findByIssueType(String issueType);
    List<Task> findByIsCodeReviewDone(Boolean isCodeReviewDone);
    List<Task> findByIsCmcDone(Boolean isCmcDone);
    List<Task> findByAssignedTeammateNamesContaining(String teammateName); // For checking if a teammate is assigned to any task

    // New: Find tasks ordered by startDate (for recent tasks)
    List<Task> findTop10ByOrderByStartDateDesc(); // Spring Data JPA automatically handles this
    // If you need to filter for uncompleted tasks, combine with isCompleted
    List<Task> findTop10ByIsCompletedFalseOrderByStartDateDesc();

    // New: Find tasks by task name containing (case-insensitive)
    List<Task> findByTaskNameContainingIgnoreCase(String taskName);

    // New: Find task by exact task name, ignoring case (for update/delete by name)
    Optional<Task> findByTaskNameIgnoreCase(String taskName);
}
