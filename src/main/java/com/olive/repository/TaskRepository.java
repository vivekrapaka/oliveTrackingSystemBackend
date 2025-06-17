package com.olive.repository;

import com.olive.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findByCurrentStage(String currentStage);
    List<Task> findByIsCompleted(Boolean isCompleted);
    List<Task> findByIssueType(String issueType);
    List<Task> findByIsCodeReviewDone(Boolean isCodeReviewDone);
    List<Task> findByIsCmcDone(Boolean isCmcDone);
    List<Task> findByAssignedTeammateNamesContaining(String teammateName);

    List<Task> findTop10ByOrderByStartDateDesc();
    List<Task> findTop10ByIsCompletedFalseOrderByStartDateDesc();

    List<Task> findByTaskNameContainingIgnoreCase(String taskName);
    Optional<Task> findByTaskNameIgnoreCase(String taskName);

    @Query("SELECT MAX(t.sequenceNumber) FROM Task t")
    Optional<Long> findMaxSequenceNumber();

    // UPDATED: Find tasks by project ID (replaces findByTeamId)
    List<Task> findByProjectId(Long projectId);

    // UPDATED: Find tasks by project ID and task name containing (case-insensitive)
    List<Task> findByProjectIdAndTaskNameContainingIgnoreCase(Long projectId, String taskName);

    // UPDATED: Find task by project ID and exact task name, ignoring case
    Optional<Task> findByProjectIdAndTaskNameIgnoreCase(Long projectId, String taskName);

    // UPDATED: Find tasks by assigned teammate name within a specific project
    List<Task> findByProjectIdAndAssignedTeammateNamesContaining(Long projectId, String teammateName);

    // UPDATED: Find tasks by completion status within a specific project
    List<Task> findByProjectIdAndIsCompleted(Long projectId, Boolean isCompleted);

    // UPDATED: Find top N uncompleted tasks by start date for a specific project
    List<Task> findTop10ByProjectIdAndIsCompletedFalseOrderByStartDateDesc(Long projectId);

    // UPDATED: Count of tasks by stage for a specific project
    @Query("SELECT t.currentStage, COUNT(t) FROM Task t WHERE t.projectId = :projectId GROUP BY t.currentStage")
    List<Object[]> countTasksByStageAndProjectId(Long projectId);

    // UPDATED: Count of tasks by issue type for a specific project
    @Query("SELECT t.issueType, COUNT(t) FROM Task t WHERE t.projectId = :projectId GROUP BY t.issueType")
    List<Object[]> countTasksByIssueTypeAndProjectId(Long projectId);

    // UPDATED: Count tasks pending code review for a specific project
    long countByProjectIdAndIsCodeReviewDoneFalse(Long projectId);

    // UPDATED: Count tasks pending CMC approval for a specific project
    long countByProjectIdAndIsCmcDoneFalse(Long projectId);

    // UPDATED: Count total tasks for a project
    long countByProjectId(Long projectId);

    // UPDATED: Count active tasks for a project (not completed and not in Prod stage)
    long countByProjectIdAndIsCompletedFalseAndCurrentStageNot(Long projectId, String stage);

}
