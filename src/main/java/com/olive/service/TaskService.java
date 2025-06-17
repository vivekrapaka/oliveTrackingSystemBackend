package com.olive.service;

import com.olive.dto.TaskCreateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TaskUpdateRequest;
import com.olive.dto.TasksSummaryResponse;
import com.olive.model.Project;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final TeammateRepository teammateRepository;
    private final ProjectRepository projectRepository; // NEW: Inject ProjectRepository
    private final TeammateService teammateService;

    private static final List<String> VALID_STAGES = List.of("SIT", "DEV", "Pre-Prod", "Prod");
    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public TaskService(TaskRepository taskRepository, TeammateRepository teammateRepository, ProjectRepository projectRepository, TeammateService teammateService) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
        this.projectRepository = projectRepository; // Initialize ProjectRepository
        this.teammateService = teammateService;
    }

    // Helper to convert Task Entity to TaskResponse DTO
    private TaskResponse convertToDto(Task task) {
        logger.debug("Starting convertToDto for Task ID: {}", task.getTaskId());

        List<String> assignedTeammates = null;
        if (task.getAssignedTeammateNames() != null && !task.getAssignedTeammateNames().isEmpty()) {
            assignedTeammates = Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toList());
            logger.debug("Parsed assigned teammates: {}", assignedTeammates);
        } else {
            logger.debug("No assigned teammates found or assignedTeammateNames is null/empty.");
        }

        String formattedTaskNumber = null;
        if (task.getSequenceNumber() != null) {
            formattedTaskNumber = String.format("TSK-%03d", task.getSequenceNumber());
            logger.debug("Formatted task number: {}", formattedTaskNumber);
        } else {
            logger.warn("Task (ID: {}) has a null sequence number. Formatted task number will be null.", task.getTaskId());
        }

        // Fetch project name
        String projectName = projectRepository.findById(task.getProjectId())
                .map(Project::getProjectName)
                .orElse("Unknown Project");

        TaskResponse response = new TaskResponse(
                task.getTaskId(),
                task.getTaskName(),
                formattedTaskNumber,
                task.getDescription(),
                task.getIssueType(),
                task.getReceivedDate(),
                task.getDevelopmentStartDate(),
                task.getCurrentStage(),
                task.getDueDate(),
                assignedTeammates,
                task.getPriority(),
                task.getIsCompleted(),
                task.getIsCmcDone(),
                task.getProjectId(), // NEW: projectId
                projectName // NEW: projectName
        );
        logger.info("Converted Task entity to DTO: {}", response);
        return response;
    }

    public Long generateNextSequenceNumber() {
        Optional<Long> maxSequenceNumberOptional = taskRepository.findMaxSequenceNumber();
        Long nextNumber = maxSequenceNumberOptional.map(max -> max + 1).orElse(1L);
        logger.info("Generated next sequence number: {}", nextNumber);
        return nextNumber;
    }

    // Get all tasks, now returning TasksSummaryResponse - UPDATED (Project-aware filtering)
    public TasksSummaryResponse getAllTasks(String taskNameFilter) {
        logger.info("Fetching all tasks with filter: {}", taskNameFilter);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Task> tasksToReturn;

        if ("ADMIN".equalsIgnoreCase(userDetails.getRole())) {
            logger.info("User is ADMIN, fetching all tasks globally.");
            if (taskNameFilter != null && !taskNameFilter.trim().isEmpty()) {
                tasksToReturn = taskRepository.findByTaskNameContainingIgnoreCase(taskNameFilter);
            } else {
                tasksToReturn = taskRepository.findAll();
            }
            logger.info("Found {} tasks matching filter '{}' globally.", tasksToReturn.size(), taskNameFilter);
        } else if (userDetails.getProjectId() != null) {
            logger.info("User is {} from project ID {}. Fetching tasks for project ID: {}", userDetails.getRole(), userDetails.getProjectId(), userDetails.getProjectId());
            if (taskNameFilter != null && !taskNameFilter.trim().isEmpty()) {
                tasksToReturn = taskRepository.findByProjectIdAndTaskNameContainingIgnoreCase(userDetails.getProjectId(), taskNameFilter);
            } else {
                tasksToReturn = taskRepository.findByProjectId(userDetails.getProjectId());
            }
            logger.info("Found {} tasks matching filter '{}' for project ID {}.", tasksToReturn.size(), taskNameFilter, userDetails.getProjectId());
        } else {
            logger.warn("User {} has role {} but no projectId assigned. Returning empty list for tasks.", userDetails.getEmail(), userDetails.getRole());
            tasksToReturn = Collections.emptyList();
        }

        long totalTasksCount = tasksToReturn.size();

        List<TaskResponse> taskResponses = tasksToReturn.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        TasksSummaryResponse summaryResponse = new TasksSummaryResponse(totalTasksCount, taskResponses);
        logger.info("Returning TasksSummaryResponse with {} tasks visible to user.", summaryResponse.getTasks().size());
        return summaryResponse;
    }

    // getTaskEntityByNameAndProject - Helper for internal use, project-aware
    private Task getTaskEntityByNameAndProject(String taskName, Long projectId) {
        Optional<Task> taskOptional;
        if (projectId == null) { // This implies ADMIN user or unassigned, ADMIN has global view.
            taskOptional = taskRepository.findByTaskNameIgnoreCase(taskName);
            logger.debug("Searching for task '{}' globally (likely Admin user).", taskName);
        } else {
            taskOptional = taskRepository.findByProjectIdAndTaskNameIgnoreCase(projectId, taskName);
            logger.debug("Searching for task '{}' within project ID {}.", taskName, projectId);
        }
        return taskOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with name: " + taskName));
    }


    // Create a new task - UPDATED (Project-aware)
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        logger.info("Received request to create task: {}", request.getTaskName());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Long projectIdForNewTask = userDetails.getProjectId();

        // Managers, BAs can only create tasks within their assigned project.
        // Admins can create tasks for any project (but for now, we'll assign to the Admin's assigned project, if any)
        // If an ADMIN has no projectId (meaning global admin), they still must assign the task to *some* project.
        // For simplicity, we enforce that any user creating a task must have a projectId.
        if (projectIdForNewTask == null) {
            logger.error("User {} (Role {}) attempted to create a task but has no projectId. Task creation requires a project assignment. Access denied.", userDetails.getEmail(), userDetails.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be assigned to a project to create tasks.");
        }

        if (!VALID_STAGES.contains(request.getCurrentStage())) {
            logger.warn("Invalid Task Stage received: {}", request.getCurrentStage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task Stage: " + request.getCurrentStage() + ". Valid stages are: " + String.join(", ", VALID_STAGES));
        }

        String nameToSave = request.getTaskName() != null ? request.getTaskName().toUpperCase() : null;

        // Check for task name uniqueness within the assigned project
        if (nameToSave != null && taskRepository.findByProjectIdAndTaskNameIgnoreCase(projectIdForNewTask, nameToSave).isPresent()) {
            logger.warn("Attempted to create task with duplicate name '{}' in project ID {}.", nameToSave, projectIdForNewTask);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task with this name (case-insensitive) already exists in this project. Please use a unique task name.");
        }

        Task task = new Task();
        task.setTaskName(nameToSave);
        task.setSequenceNumber(generateNextSequenceNumber());
        task.setDescription(request.getDescription());
        task.setCurrentStage(request.getCurrentStage());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setIssueType(request.getIssueType());
        task.setReceivedDate(request.getReceivedDate());
        task.setDevelopmentStartDate(request.getDevelopmentStartDate());
        task.setPriority(request.getPriority());
        task.setProjectId(projectIdForNewTask); // Assign projectId from authenticated user

        // Handle assigned teammates by name
        if (request.getAssignedTeammateNames() != null && !request.getAssignedTeammateNames().isEmpty()) {
            Set<String> uniqueAssignedNames = new HashSet<>();
            for (String teammateName : request.getAssignedTeammateNames()) {
                Teammate teammate = teammateRepository.findByNameIgnoreCaseAndProjectId(teammateName, projectIdForNewTask) // Look up within project
                        .orElseThrow(() -> {
                            logger.warn("Teammate '{}' not found in project ID {} during task creation.", teammateName, projectIdForNewTask);
                            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate not found with name: " + teammateName + " in your project.");
                        });
                uniqueAssignedNames.add(teammate.getName());
            }
            task.setAssignedTeammateNames(String.join(ASSIGNED_NAMES_DELIMITER, uniqueAssignedNames));
            logger.debug("Assigned teammates for new task: {}", task.getAssignedTeammateNames());
        } else {
            task.setAssignedTeammateNames("");
            logger.debug("No teammates assigned to new task.");
        }

        logger.info("Saving new task entity: Task Name={}, Stage={}, DueDate={}, AssignedTeammates={}, ProjectId={}",
                task.getTaskName(), task.getCurrentStage(), task.getDueDate(), task.getAssignedTeammateNames(), task.getProjectId());
        Task savedTask = taskRepository.save(task);
        logger.info("New task saved successfully with ID: {}", savedTask.getTaskId());

        if (savedTask.getAssignedTeammateNames() != null && !savedTask.getAssignedTeammateNames().isEmpty()) {
            Arrays.stream(savedTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .forEach(teammateService::updateTeammateAvailability);
            logger.info("Updated availability for assigned teammates after task creation.");
        }

        return convertToDto(savedTask);
    }

    // Update an existing task by name - UPDATED (Project-aware RBAC)
    @Transactional
    public TaskResponse updateTask(String name, TaskUpdateRequest request) {
        logger.info("Received request to update task: {}", name);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Find existing task, scoped by project if not ADMIN
        Task existingTask = getTaskEntityByNameAndProject(name, "ADMIN".equalsIgnoreCase(userDetails.getRole()) ? null : userDetails.getProjectId());

        logger.debug("Found existing task with ID: {}", existingTask.getTaskId());

        // Authorization check: MANAGER, BA can update any task in their project.
        // TEAM_MEMBER has granular restrictions.
        if (!"ADMIN".equalsIgnoreCase(userDetails.getRole()) && !existingTask.getProjectId().equals(userDetails.getProjectId())) {
            logger.warn("User {} (Role {}, Project {}) attempted to update task {} (Project {}). Access denied.",
                    userDetails.getEmail(), userDetails.getRole(), userDetails.getProjectId(), existingTask.getTaskName(), existingTask.getProjectId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You can only update tasks within your assigned project.");
        }

        // TEAM_MEMBER specific restrictions
        if ("TEAM_MEMBER".equalsIgnoreCase(userDetails.getRole())) {
            Set<String> assignedTeammates = new HashSet<>();
            if (existingTask.getAssignedTeammateNames() != null && !existingTask.getAssignedTeammateNames().isEmpty()) {
                assignedTeammates.addAll(Arrays.stream(existingTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                        .map(String::trim)
                        .collect(Collectors.toSet()));
            }

            // A TEAM_MEMBER can only update tasks if they are assigned to it.
            if (!assignedTeammates.contains(userDetails.getFullName().toUpperCase())) {
                logger.warn("TEAM_MEMBER {} attempted to update task {} but is not assigned to it. Access denied.", userDetails.getEmail(), existingTask.getTaskName());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You can only update tasks you are assigned to.");
            }

            // Restrict fields TEAM_MEMBER can update as per requirements.
            // They can update isCompleted, isCmcDone, currentStage, description.
            // All other fields are forbidden.
            if (request.getTaskName() != null && !request.getTaskName().equalsIgnoreCase(existingTask.getTaskName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TEAM_MEMBERs cannot change task names.");
            }
            if (request.getPriority() != null && !request.getPriority().equals(existingTask.getPriority())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TEAM_MEMBERs cannot change task priority.");
            }
            if (request.getStartDate() != null && !request.getStartDate().equals(existingTask.getStartDate())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TEAM_MEMBERs cannot change task start date.");
            }
            if (request.getDueDate() != null && !request.getDueDate().equals(existingTask.getDueDate())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TEAM_MEMBERs cannot change task due date.");
            }
            if (request.getIssueType() != null && !request.getIssueType().equals(existingTask.getIssueType())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TEAM_MEMBERs cannot change task issue type.");
            }
            // Check if assignedTeammateNames is modified
            if (request.getAssignedTeammateNames() != null) {
                Set<String> requestedAssignedNames = request.getAssignedTeammateNames().stream().map(String::toUpperCase).collect(Collectors.toSet());
                Set<String> currentAssignedNames = Arrays.stream(existingTask.getAssignedTeammateNames().split(","))
                        .map(String::trim).map(String::toUpperCase).collect(Collectors.toSet());
                if (!requestedAssignedNames.equals(currentAssignedNames)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "TEAM_MEMBERs cannot change assigned teammates.");
                }
            }
        }


        Set<String> oldAssignedNames = new HashSet<>();
        if (existingTask.getAssignedTeammateNames() != null && !existingTask.getAssignedTeammateNames().isEmpty()) {
            oldAssignedNames.addAll(Arrays.stream(existingTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }
        Boolean wasCompleted = existingTask.getIsCompleted();

        // Handle task name update: convert to uppercase and check for uniqueness within project
        if (request.getTaskName() != null && !request.getTaskName().equalsIgnoreCase(existingTask.getTaskName())) {
            String newNameToSave = request.getTaskName().toUpperCase();
            Optional<Task> taskWithNewName = taskRepository.findByProjectIdAndTaskNameIgnoreCase(existingTask.getProjectId(), newNameToSave);
            if (taskWithNewName.isPresent() && !taskWithNewName.get().getTaskId().equals(existingTask.getTaskId())) {
                logger.warn("Attempted to update task name to a duplicate '{}' in project ID {}.", newNameToSave, existingTask.getProjectId());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Task with new name '" + request.getTaskName() + "' (case-insensitive) already exists in this project.");
            }
            existingTask.setTaskName(newNameToSave);
            logger.info("Task name updated to: {}", newNameToSave);
        }

        // Update other fields if provided
        Optional.ofNullable(request.getDescription()).ifPresent(existingTask::setDescription);
        if (request.getCurrentStage() != null) {
            if (!VALID_STAGES.contains(request.getCurrentStage())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task Stage: " + request.getCurrentStage() + ". Valid stages are: " + String.join(", ", VALID_STAGES));
            }
            existingTask.setCurrentStage(request.getCurrentStage());
        }
        Optional.ofNullable(request.getStartDate()).ifPresent(existingTask::setStartDate);
        Optional.ofNullable(request.getDueDate()).ifPresent(existingTask::setDueDate);
        Optional.ofNullable(request.getIsCompleted()).ifPresent(existingTask::setIsCompleted);
        Optional.ofNullable(request.getIssueType()).ifPresent(existingTask::setIssueType);
        Optional.ofNullable(request.getReceivedDate()).ifPresent(existingTask::setReceivedDate);
        Optional.ofNullable(request.getDevelopmentStartDate()).ifPresent(existingTask::setDevelopmentStartDate);
        Optional.ofNullable(request.getIsCodeReviewDone()).ifPresent(existingTask::setIsCodeReviewDone);
        Optional.ofNullable(request.getIsCmcDone()).ifPresent(existingTask::setIsCmcDone);
        Optional.ofNullable(request.getPriority()).ifPresent(existingTask::setPriority);

        // Handle assigned teammates update (only if not TEAM_MEMBER or if TEAM_MEMBER didn't try to change it)
        Set<String> newAssignedNames = null;
        if (!"TEAM_MEMBER".equalsIgnoreCase(userDetails.getRole()) || request.getAssignedTeammateNames() == null ||
                (request.getAssignedTeammateNames().stream().map(String::toUpperCase).collect(Collectors.toSet())
                        .equals(Arrays.stream(existingTask.getAssignedTeammateNames().split(","))
                                .map(String::trim).map(String::toUpperCase).collect(Collectors.toSet()))
                )) { // Only proceed if TEAM_MEMBER didn't try to change, or if it's not a TEAM_MEMBER

            newAssignedNames = new HashSet<>();
            if (request.getAssignedTeammateNames() != null) {
                for (String teammateName : request.getAssignedTeammateNames()) {
                    Teammate teammate = teammateRepository.findByNameIgnoreCaseAndProjectId(teammateName, existingTask.getProjectId()) // Look up within task's project
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate not found with name: " + teammateName + " in this project."));
                    newAssignedNames.add(teammate.getName());
                }
                existingTask.setAssignedTeammateNames(String.join(ASSIGNED_NAMES_DELIMITER, newAssignedNames));
            } else {
                existingTask.setAssignedTeammateNames("");
            }
        } else {
            logger.debug("TEAM_MEMBER attempted to modify assignedTeammateNames. This is forbidden.");
        }


        Task updatedTask = taskRepository.save(existingTask);

        Set<String> allAffectedTeammates = new HashSet<>(oldAssignedNames);
        if (updatedTask.getAssignedTeammateNames() != null && !updatedTask.getAssignedTeammateNames().isEmpty()) {
            allAffectedTeammates.addAll(Arrays.stream(updatedTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        if (!Objects.equals(wasCompleted, updatedTask.getIsCompleted()) || !oldAssignedNames.equals(newAssignedNames)) {
            for (String teammateName : allAffectedTeammates) {
                teammateService.updateTeammateAvailability(teammateName);
            }
        }

        return convertToDto(updatedTask);
    }

    // Delete a task by name - UPDATED (Project-aware RBAC)
    @Transactional
    public void deleteTask(String name) {
        logger.info("Received request to delete task: {}", name);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Find existing task, scoped by project if not ADMIN
        Task taskToDelete = getTaskEntityByNameAndProject(name, "ADMIN".equalsIgnoreCase(userDetails.getRole()) ? null : userDetails.getProjectId());

        logger.debug("Found task to delete with ID: {}", taskToDelete.getTaskId());

        // Authorization check: MANAGER, BA can delete any task in their project.
        if (!"ADMIN".equalsIgnoreCase(userDetails.getRole()) && !taskToDelete.getProjectId().equals(userDetails.getProjectId())) {
            logger.warn("User {} (Role {}, Project {}) attempted to delete task {} (Project {}). Access denied.",
                    userDetails.getEmail(), userDetails.getRole(), userDetails.getProjectId(), taskToDelete.getTaskName(), taskToDelete.getProjectId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You can only delete tasks within your assigned project.");
        }

        Set<String> affectedTeammates = new HashSet<>();
        if (taskToDelete.getAssignedTeammateNames() != null && !taskToDelete.getAssignedTeammateNames().isEmpty()) {
            affectedTeammates.addAll(Arrays.stream(taskToDelete.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        taskRepository.delete(taskToDelete);
        logger.info("Task '{}' deleted successfully.", name);

        for (String teammateName : affectedTeammates) {
            teammateService.updateTeammateAvailability(teammateName);
        }
        logger.info("Updated availability for affected teammates after task deletion.");
    }
}
