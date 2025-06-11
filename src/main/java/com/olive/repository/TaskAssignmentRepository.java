package com.olive.repository;

import com.olive.model.Task;
import com.olive.model.TaskAssignment;
import com.olive.model.Teammate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment,Long> {
    List<TaskAssignment> findByTask(Task task);
    List<TaskAssignment> findByTeammate(Teammate teammate);
    List<TaskAssignment> findByIsActive(Boolean isActive);
    Optional<TaskAssignment> findByTaskAndTeammate(Task task, Teammate teammate);
}
