package com.olive.repository;

import com.olive.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project,Long> {
    Optional<Project> findByProjectNameIgnoreCase(String projectName);

    boolean existsByProjectNameIgnoreCase(String newProjectNameToSave);
}
