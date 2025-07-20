package com.olive.repository;

import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.model.enums.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // FIX: Updated query to fetch both developer and tester collections.
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.assignedDevelopers LEFT JOIN FETCH t.assignedTesters WHERE t.project.id IN :projectIds")
    List<Task> findByProjectIdInWithTeammates(@Param("projectIds") List<Long> projectIds);

    // FIX: Updated query for the findAll case.
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.assignedDevelopers LEFT JOIN FETCH t.assignedTesters")
    List<Task> findAllWithTeammates();

    Optional<Task> findByProjectIdAndTaskNameIgnoreCase(Long projectId, String taskName);
    List<Task> findByTaskNameContainingIgnoreCase(String taskName);
    List<Task> findTop5ByOrderByDevelopmentStartDateDesc();

    @Query("SELECT t.sequenceNumber FROM Task t WHERE t.parentTask IS NULL")
    List<String> findAllParentSequenceNumbers();

    long countByParentTask(Task parentTask);

    @Query("SELECT COUNT(t) FROM Task t WHERE :teammate MEMBER OF t.assignedDevelopers OR :teammate MEMBER OF t.assignedTesters")
    long countTasksByTeammate(@Param("teammate") Teammate teammate);

    @Query("SELECT t FROM Task t WHERE :teammate MEMBER OF t.assignedDevelopers OR :teammate MEMBER OF t.assignedTesters")
    List<Task> findTasksByTeammate(@Param("teammate") Teammate teammate);

    List<Task> findByProjectIdIn(List<Long> userProjectIds);

    List<Task> findByProjectIdInAndTaskType(List<Long> projectIds, TaskType taskType);

    List<Task> findByTaskTypeNot(TaskType taskType);

}