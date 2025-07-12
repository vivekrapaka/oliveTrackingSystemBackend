package com.olive.repository;

import com.olive.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByProjectIdAndTaskNameIgnoreCase(Long projectId, String taskName);
    List<Task> findByProjectIdIn(List<Long> projectIds);
    List<Task> findByProjectIdInAndTaskNameContainingIgnoreCase(List<Long> projectIds, String taskName);
    List<Task> findByTaskNameContainingIgnoreCase(String taskName);
    List<Task> findTop5ByOrderByDevelopmentStartDateDesc();

    // FIX: This method was missing in the previous update. It is now correctly defined.
    @Query("SELECT t.sequenceNumber FROM Task t WHERE t.parentTask IS NULL")
    List<String> findAllParentSequenceNumbers();

    long countByParentTask(Task parentTask);
}
