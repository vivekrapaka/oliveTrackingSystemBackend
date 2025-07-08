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

    // FIX: Changed to a native query to use database-specific functions reliably.
    @Query(value = "SELECT MAX(CAST(SUBSTRING(sequence_number, 1, CHARINDEX('.', sequence_number + '.') - 1) AS int)) FROM tasks WHERE parent_task_id IS NULL", nativeQuery = true)
    Optional<Integer> findMaxParentSequenceNumber();

    long countByParentTask(Task parentTask);
}
