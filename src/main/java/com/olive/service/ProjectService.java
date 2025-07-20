package com.olive.service;

import com.olive.dto.ProjectCreateRequest;
import com.olive.dto.ProjectResponse;
import com.olive.model.Project;
import com.olive.model.Task;
import com.olive.model.enums.TaskStatus;
import com.olive.model.enums.TaskType;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TaskRepository;
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
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        logger.info("Received request to create project: {}", request.getProjectName());
        String projectNameToSave = request.getProjectName().trim().toUpperCase();

        if (projectRepository.existsByProjectNameIgnoreCase(projectNameToSave)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project with this name already exists.");
        }

        Project project = new Project();
        project.setProjectName(projectNameToSave);
        project.setDescription(request.getDescription());

        Project savedProject = projectRepository.save(project);
        logger.info("Project created successfully with ID: {}", savedProject.getProjectId());

        createGeneralActivityTasksForProject(savedProject);

        return convertToDto(savedProject);
    }

    private void createGeneralActivityTasksForProject(Project project) {
        // FIX: Simplified to only the two required general tasks.
        String[] generalTaskNames = {
                "General - Analysis",
                "General - Other Work"
        };

        for (String taskName : generalTaskNames) {
            Task activityTask = new Task();
            activityTask.setTaskName(taskName);
            activityTask.setProject(project);
            activityTask.setTaskType(TaskType.GENERAL_ACTIVITY);
            activityTask.setStatus(TaskStatus.BACKLOG);
            activityTask.setPriority("Low");
            activityTask.setSequenceNumber(taskService.generateNextSequenceNumber(null));
            taskRepository.save(activityTask);
            logger.info("Created general activity task '{}' for project '{}'", taskName, project.getProjectName());
        }
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + id));
        return convertToDto(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectCreateRequest request) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + id));
        String newProjectNameToSave = request.getProjectName().trim().toUpperCase();
        if (!newProjectNameToSave.equalsIgnoreCase(existingProject.getProjectName())) {
            if (projectRepository.existsByProjectNameIgnoreCase(newProjectNameToSave)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Project with new name '" + request.getProjectName() + "' already exists.");
            }
            existingProject.setProjectName(newProjectNameToSave);
        }
        Optional.ofNullable(request.getDescription()).ifPresent(existingProject::setDescription);
        Project updatedProject = projectRepository.save(existingProject);
        return convertToDto(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    private ProjectResponse convertToDto(Project project) {
        return new ProjectResponse(project.getProjectId(), project.getProjectName(), project.getDescription());
    }
}
