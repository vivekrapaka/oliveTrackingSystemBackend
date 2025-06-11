package com.olive.repository;

import com.olive.model.TaskStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStageRepository extends JpaRepository<TaskStage,Integer> {
    // Custom query to find a task stage by its name
    Optional<TaskStage> findByStageName(String stageName);
}
