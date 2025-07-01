package com.olive.service;

import com.olive.dto.ProjectCreateRequest;
import com.olive.dto.ProjectResponse;
import com.olive.model.Project;
import com.olive.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    private ProjectResponse convertToDto(Project project) {
        return new ProjectResponse(project.getId(), project.getProjectName(), project.getDescription());
    }

    public List<ProjectResponse> getAllProjects() {
        logger.info("Fetching all projects.");
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id) {
        logger.info("Attempting to retrieve project with ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Project not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + id);
                });
        return convertToDto(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        logger.info("Received request to create project: {}", request.getProjectName());
        String projectNameToSave = request.getProjectName().trim().toUpperCase();

        if (projectRepository.findByProjectNameIgnoreCase(projectNameToSave).isPresent()) {
            logger.warn("Attempted to create project with duplicate name: {}", projectNameToSave);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project with this name already exists.");
        }

        Project project = new Project();
        project.setProjectName(projectNameToSave);
        project.setDescription(request.getDescription());

        Project savedProject = projectRepository.save(project);
        logger.info("Project created successfully with ID: {}", savedProject.getId());
        return convertToDto(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectCreateRequest request) {
        logger.info("Received request to update project with ID: {}", id);
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Project not found for update with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + id);
                });

        String newProjectNameToSave = request.getProjectName().trim().toUpperCase();

        if (!newProjectNameToSave.equalsIgnoreCase(existingProject.getProjectName())) {
            if (projectRepository.findByProjectNameIgnoreCase(newProjectNameToSave).isPresent()) {
                logger.warn("Attempted to update project name to a duplicate: {}", newProjectNameToSave);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Project with new name '" + request.getProjectName() + "' already exists.");
            }
            existingProject.setProjectName(newProjectNameToSave);
            logger.info("Project name updated to: {}", newProjectNameToSave);
        }

        Optional.ofNullable(request.getDescription()).ifPresent(existingProject::setDescription);

        Project updatedProject = projectRepository.save(existingProject);
        logger.info("Project with ID {} updated successfully.", updatedProject.getId());
        return convertToDto(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        logger.info("Received request to delete project with ID: {}", id);
        if (!projectRepository.existsById(id)) {
            logger.warn("Project not found for deletion with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + id);
        }
        // TODO: Add checks for associated tasks and teammates before deleting a project
        // For now, it will likely fail on FK constraint. Implement logic here to disassociate or prevent deletion.
        projectRepository.deleteById(id);
        logger.info("Project with ID {} deleted successfully.", id);
    }
}
