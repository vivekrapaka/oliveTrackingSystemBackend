package com.olive.service;

import com.olive.dto.TaskCreateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TaskUpdateRequest;
import com.olive.dto.TasksSummaryResponse;

import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final TeammateRepository teammateRepository; // Inject TeammateRepository
    private final TeammateService teammateService; // Inject TeammateService

    // Hardcoded list of valid task stages
    private static final List<String> VALID_STAGES = List.of("SIT", "DEV", "Pre-Prod", "Prod","FSD","UAT","HOLD","Completed");
    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public TaskService(TaskRepository taskRepository, TeammateRepository teammateRepository, TeammateService teammateService) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
        this.teammateService = teammateService;
    }

    // Helper to convert Task Entity to TaskResponse DTO
    private TaskResponse convertToDto(Task task) {
        logger.debug("Starting convertToDto for Task ID: {}", task.getTaskId());
        logger.debug("Task Name from entity: {}", task.getTaskName());
        logger.debug("Sequence Number from entity: {}", task.getSequenceNumber());
        logger.debug("Description from entity: {}", task.getDescription());
        logger.debug("Issue Type from entity: {}", task.getIssueType());
        logger.debug("Received Date from entity: {}", task.getReceivedDate());
        logger.debug("Development Start Date from entity: {}", task.getDevelopmentStartDate());
        logger.debug("Current Stage from entity: {}", task.getCurrentStage());
        logger.debug("Due Date from entity: {}", task.getDueDate());
        logger.debug("Assigned Teammate Names from entity: {}", task.getAssignedTeammateNames());
        logger.debug("Priority from entity: {}", task.getPriority());
        logger.debug("Is Completed from entity: {}", task.getIsCompleted());
        logger.debug("Is CMC Done from entity: {}", task.getIsCmcDone());


        List<String> assignedTeammates = null;
        if (task.getAssignedTeammateNames() != null && !task.getAssignedTeammateNames().isEmpty()) {
            assignedTeammates = Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toList());
            logger.debug("Parsed assigned teammates: {}", assignedTeammates);
        } else {
            logger.debug("No assigned teammates found or assignedTeammateNames is null/empty.");
        }

        // Format sequenceNumber to TSK-XXX string
        String formattedTaskNumber = null;
        if (task.getSequenceNumber() != null) {
            formattedTaskNumber = String.format("TSK-%03d", task.getSequenceNumber());
            logger.debug("Formatted task number: {}", formattedTaskNumber);
        } else {
            logger.warn("Task (ID: {}) has a null sequence number. Formatted task number will be null.", task.getTaskId());
        }

        TaskResponse response = new TaskResponse(
                task.getTaskId(), // maps to id
                task.getTaskName(), // maps to name
                formattedTaskNumber, // NEW: formatted task number
                task.getDescription(), // NEW: description
                task.getIssueType(),
                task.getReceivedDate(),
                task.getDevelopmentStartDate(),
                task.getCurrentStage(),
                task.getDueDate(),
                assignedTeammates, // maps to assignedTeammates
                task.getPriority(),
                task.getIsCompleted(),
                task.getIsCmcDone() // maps to isCmcDone
        );
        logger.info("Converted Task entity to DTO: {}", response);
        return response;
    }

    // Generate a new unique sequence number
    public Long generateNextSequenceNumber() {
        // Find the maximum existing sequence number
        Optional<Long> maxSequenceNumberOptional = taskRepository.findMaxSequenceNumber();
        // If there are no tasks yet, start from 1, otherwise increment the max
        Long nextNumber = maxSequenceNumberOptional.map(max -> max + 1).orElse(1L);
        logger.info("Generated next sequence number: {}", nextNumber);
        return nextNumber;
    }


    // Get all tasks, now returning TasksSummaryResponse
    public TasksSummaryResponse getAllTasks(String taskNameFilter) {
        logger.info("Fetching all tasks with filter: {}", taskNameFilter);
        List<Task> allTasks;
        if (taskNameFilter != null && !taskNameFilter.trim().isEmpty()) {
            allTasks = taskRepository.findByTaskNameContainingIgnoreCase(taskNameFilter);
            logger.info("Found {} tasks matching filter '{}'", allTasks.size(), taskNameFilter);
        } else {
            allTasks = taskRepository.findAll();
            logger.info("Found {} total tasks.", allTasks.size());
        }

        long totalTasksCount = allTasks.size(); // Total count of tasks after applying filter (if any)

        List<TaskResponse> taskResponses = allTasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        TasksSummaryResponse summaryResponse = new TasksSummaryResponse(totalTasksCount, taskResponses);
        logger.info("Returning TasksSummaryResponse with {} tasks.", summaryResponse.getTasks().size());
        return summaryResponse;
    }


    // getTaskById remains but is now private/internal as it's not exposed via an API endpoint anymore
    public TaskResponse getTaskById(Long id) {
        logger.info("Attempting to retrieve task with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + id));
        logger.info("Successfully retrieved task: {}", task.getTaskName());
        return convertToDto(task);
    }

    // Create a new task
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        logger.info("Received request to create task: {}", request.getTaskName());
        // Validate Task Stage against hardcoded list
        if (!VALID_STAGES.contains(request.getCurrentStage())) {
            logger.warn("Invalid Task Stage received: {}", request.getCurrentStage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task Stage: " + request.getCurrentStage() + ". Valid stages are: " + String.join(", ", VALID_STAGES));
        }

        // Convert name to uppercase for uniqueness check and storage consistency
        String nameToSave = request.getTaskName() != null ? request.getTaskName().toUpperCase() : null;
        logger.debug("Task name to save (uppercase): {}", nameToSave);

        // Check for task name uniqueness (case-insensitive)
        if (nameToSave != null && taskRepository.findByTaskNameIgnoreCase(nameToSave).isPresent()) {
            logger.warn("Attempted to create task with duplicate name: {}", nameToSave);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task with this name (case-insensitive) already exists. Please use a unique task name.");
        }

        Task task = new Task();
        task.setTaskName(nameToSave); // Set the uppercase name
        task.setSequenceNumber(generateNextSequenceNumber()); // Automatically generate and assign sequence number
        task.setDescription(request.getDescription());
        task.setCurrentStage(request.getCurrentStage());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setIssueType(request.getIssueType());
        task.setReceivedDate(request.getReceivedDate());
        task.setDevelopmentStartDate(request.getDevelopmentStartDate());
        task.setPriority(request.getPriority()); // Set priority
        // isCompleted, isCodeReviewDone, isCmcDone default to false in entity

        // Handle assigned teammates by name
        if (request.getAssignedTeammateNames() != null && !request.getAssignedTeammateNames().isEmpty()) {
            Set<String> uniqueAssignedNames = new HashSet<>();
            for (String teammateName : request.getAssignedTeammateNames()) {
                // Lookup teammate using case-insensitive name
                Teammate teammate = teammateRepository.findByNameIgnoreCase(teammateName)
                        .orElseThrow(() -> {
                            logger.warn("Teammate not found during task creation: {}", teammateName);
                            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate not found with name: " + teammateName);
                        });
                // Add to unique names set to avoid duplicates in string (store uppercase in task as well)
                uniqueAssignedNames.add(teammate.getName()); // Teammate.getName() will return uppercase
            }
            task.setAssignedTeammateNames(String.join(ASSIGNED_NAMES_DELIMITER, uniqueAssignedNames));
            logger.debug("Assigned teammates for new task: {}", task.getAssignedTeammateNames());
        } else {
            task.setAssignedTeammateNames(""); // Ensure it's not null
            logger.debug("No teammates assigned to new task.");
        }

        logger.info("Saving new task entity: Task Name={}, Stage={}, DueDate={}, AssignedTeammates={}",
                task.getTaskName(), task.getCurrentStage(), task.getDueDate(), task.getAssignedTeammateNames());
        Task savedTask = taskRepository.save(task);
        logger.info("New task saved successfully with ID: {}", savedTask.getTaskId());

        // Update availability of newly assigned teammates after task is saved
        if (savedTask.getAssignedTeammateNames() != null && !savedTask.getAssignedTeammateNames().isEmpty()) {
            Arrays.stream(savedTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .forEach(teammateService::updateTeammateAvailability); // Use injected TeammateService
            logger.info("Updated availability for assigned teammates after task creation.");
        }

        return convertToDto(savedTask);
    }

    // Update an existing task by name (formerly by ID)
    @Transactional
    public TaskResponse updateTask(String name, TaskUpdateRequest request) {
        logger.info("Received request to update task: {}", name);
        // Find existing task using case-insensitive name
        Task existingTask = taskRepository.findByTaskNameIgnoreCase(name)
                .orElseThrow(() -> {
                    logger.warn("Task not found for update with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with name: " + name);
                });

        logger.debug("Found existing task with ID: {}", existingTask.getTaskId());

        // Keep track of old assigned teammates for availability update
        Set<String> oldAssignedNames = new HashSet<>();
        if (existingTask.getAssignedTeammateNames() != null && !existingTask.getAssignedTeammateNames().isEmpty()) {
            oldAssignedNames.addAll(Arrays.stream(existingTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }
        logger.debug("Old assigned teammates: {}", oldAssignedNames);

        // Store original completion status
        Boolean wasCompleted = existingTask.getIsCompleted();
        logger.debug("Original task completion status: {}", wasCompleted);

        // Handle task name update: convert to uppercase and check for uniqueness
        if (request.getTaskName() != null && !request.getTaskName().equalsIgnoreCase(existingTask.getTaskName())) {
            String newNameToSave = request.getTaskName().toUpperCase();
            Optional<Task> taskWithNewName = taskRepository.findByTaskNameIgnoreCase(newNameToSave);
            if (taskWithNewName.isPresent() && !taskWithNewName.get().getTaskId().equals(existingTask.getTaskId())) {
                logger.warn("Attempted to update task name to a duplicate: {}", newNameToSave);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Task with new name '" + request.getTaskName() + "' (case-insensitive) already exists.");
            }
            existingTask.setTaskName(newNameToSave);
            logger.info("Task name updated to: {}", newNameToSave);
        }

        // Update other fields if provided
        Optional.ofNullable(request.getDescription()).ifPresent(val -> { existingTask.setDescription(val); logger.debug("Updated description."); });

        if (request.getCurrentStage() != null) {
            if (!VALID_STAGES.contains(request.getCurrentStage())) {
                logger.warn("Invalid Task Stage received for update: {}", request.getCurrentStage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task Stage: " + request.getCurrentStage() + ". Valid stages are: " + String.join(", ", VALID_STAGES));
            }
            existingTask.setCurrentStage(request.getCurrentStage());
            logger.debug("Updated current stage to: {}", request.getCurrentStage());
        }

        Optional.ofNullable(request.getStartDate()).ifPresent(val -> { existingTask.setStartDate(val); logger.debug("Updated start date."); });
        Optional.ofNullable(request.getDueDate()).ifPresent(val -> { existingTask.setDueDate(val); logger.debug("Updated due date."); });
        Optional.ofNullable(request.getIsCompleted()).ifPresent(val -> { existingTask.setIsCompleted(val); logger.debug("Updated isCompleted to: {}", val); });
        Optional.ofNullable(request.getIssueType()).ifPresent(val -> { existingTask.setIssueType(val); logger.debug("Updated issue type."); });
        Optional.ofNullable(request.getReceivedDate()).ifPresent(val -> { existingTask.setReceivedDate(val); logger.debug("Updated received date."); });
        Optional.ofNullable(request.getDevelopmentStartDate()).ifPresent(val -> { existingTask.setDevelopmentStartDate(val); logger.debug("Updated development start date."); });
        Optional.ofNullable(request.getIsCodeReviewDone()).ifPresent(val -> { existingTask.setIsCodeReviewDone(val); logger.debug("Updated isCodeReviewDone to: {}", val); });
        Optional.ofNullable(request.getIsCmcDone()).ifPresent(val -> { existingTask.setIsCmcDone(val); logger.debug("Updated isCmcDone to: {}", val); });
        Optional.ofNullable(request.getPriority()).ifPresent(val -> { existingTask.setPriority(val); logger.debug("Updated priority."); });


        // Handle assigned teammates update
        Set<String> newAssignedNames = new HashSet<>();
        if (request.getAssignedTeammateNames() != null) {
            for (String teammateName : request.getAssignedTeammateNames()) {
                Teammate teammate = teammateRepository.findByNameIgnoreCase(teammateName)
                        .orElseThrow(() -> {
                            logger.warn("Teammate not found during task update: {}", teammateName);
                            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate not found with name: " + teammateName);
                        });
                newAssignedNames.add(teammate.getName()); // Ensure uppercase and correct name is used
            }
            existingTask.setAssignedTeammateNames(String.join(ASSIGNED_NAMES_DELIMITER, newAssignedNames));
            logger.debug("New assigned teammates for task: {}", existingTask.getAssignedTeammateNames());
        } else {
            existingTask.setAssignedTeammateNames("");
            logger.debug("No teammates assigned to task after update.");
        }

        logger.info("Saving updated task entity with ID: {}", existingTask.getTaskId());
        Task updatedTask = taskRepository.save(existingTask);
        logger.info("Task updated successfully.");

        // Collect all unique teammate names involved in this task (old and new assignments)
        Set<String> allAffectedTeammates = new HashSet<>(oldAssignedNames);
        allAffectedTeammates.addAll(newAssignedNames);
        logger.debug("All affected teammates for availability update: {}", allAffectedTeammates);

        // If the task completion status changed, or assignments changed, update availability for all affected teammates
        if (!Objects.equals(wasCompleted, updatedTask.getIsCompleted()) || !oldAssignedNames.equals(newAssignedNames)) {
            logger.info("Task completion status or assigned teammates changed. Updating availability for affected teammates.");
            for (String teammateName : allAffectedTeammates) {
                teammateService.updateTeammateAvailability(teammateName); // Use injected TeammateService
            }
        }

        return convertToDto(updatedTask);
    }

    // Delete a task by name (formerly by ID)
    @Transactional
    public void deleteTask(String name) {
        logger.info("Received request to delete task: {}", name);
        Task taskToDelete = taskRepository.findByTaskNameIgnoreCase(name)
                .orElseThrow(() -> {
                    logger.warn("Task not found for deletion with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with name: " + name);
                });

        // Get assigned teammates from the task to be deleted
        Set<String> affectedTeammates = new HashSet<>();
        if (taskToDelete.getAssignedTeammateNames() != null && !taskToDelete.getAssignedTeammateNames().isEmpty()) {
            affectedTeammates.addAll(Arrays.stream(taskToDelete.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }
        logger.debug("Affected teammates for deletion: {}", affectedTeammates);

        taskRepository.delete(taskToDelete);
        logger.info("Task '{}' deleted successfully.", name);

        // Update availability of affected teammates after deletion
        for (String teammateName : affectedTeammates) {
            teammateService.updateTeammateAvailability(teammateName); // Use injected TeammateService
        }
        logger.info("Updated availability for affected teammates after task deletion.");
    }
}
