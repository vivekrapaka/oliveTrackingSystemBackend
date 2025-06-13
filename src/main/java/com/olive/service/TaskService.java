package com.olive.service;

import com.olive.dto.TaskCreateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TaskUpdateRequest;
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

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TeammateRepository teammateRepository;

    // Hardcoded list of valid task stages
    private static final List<String> VALID_STAGES = List.of("SIT", "DEV", "Pre-Prod", "Prod");
    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public TaskService(TaskRepository taskRepository, TeammateRepository teammateRepository) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
    }

    // Helper to convert Task Entity to TaskResponse DTO
    private TaskResponse convertToDto(Task task) {
        List<String> assignedTeammateNames = null;
        if (task.getAssignedTeammateNames() != null && !task.getAssignedTeammateNames().isEmpty()) {
            assignedTeammateNames = Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return new TaskResponse(
                task.getTaskId(),
                task.getTaskName(),
                task.getDescription(),
                task.getCurrentStage(), // Directly use String
                task.getStartDate(),
                task.getDueDate(),
                task.getIsCompleted(),
                task.getIssueType(),
                task.getReceivedDate(),
                task.getDevelopmentStartDate(),
                task.getIsCodeReviewDone(),
                task.getIsCmcDone(),
                assignedTeammateNames,
                task.getPriority() // Include priority
        );
    }

    // Get all tasks
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get task by ID
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + id));
        return convertToDto(task);
    }

    // Create a new task
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        // Validate Task Stage against hardcoded list
        if (!VALID_STAGES.contains(request.getCurrentStage())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task Stage: " + request.getCurrentStage() + ". Valid stages are: " + String.join(", ", VALID_STAGES));
        }

        Task task = new Task();
        task.setTaskName(request.getTaskName());
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
                Teammate teammate = teammateRepository.findByName(teammateName)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate not found with name: " + teammateName));
                // Add to unique names set to avoid duplicates in string
                uniqueAssignedNames.add(teammate.getName());
            }
            task.setAssignedTeammateNames(String.join(ASSIGNED_NAMES_DELIMITER, uniqueAssignedNames));
        } else {
            task.setAssignedTeammateNames(""); // Ensure it's not null
        }

        Task savedTask = taskRepository.save(task);

        // Update availability of newly assigned teammates after task is saved
        if (savedTask.getAssignedTeammateNames() != null && !savedTask.getAssignedTeammateNames().isEmpty()) {
            Arrays.stream(savedTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .forEach(this::updateTeammateAvailability);
        }

        return convertToDto(savedTask);
    }

    // Update an existing task
    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + id));

        // Keep track of old assigned teammates for availability update
        Set<String> oldAssignedNames = new HashSet<>();
        if (existingTask.getAssignedTeammateNames() != null && !existingTask.getAssignedTeammateNames().isEmpty()) {
            oldAssignedNames.addAll(Arrays.stream(existingTask.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        // Store original completion status
        Boolean wasCompleted = existingTask.getIsCompleted();

        // Update basic fields if provided
        Optional.ofNullable(request.getTaskName()).ifPresent(existingTask::setTaskName);
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
        Optional.ofNullable(request.getPriority()).ifPresent(existingTask::setPriority); // Set priority

        // Handle assigned teammates update
        Set<String> newAssignedNames = new HashSet<>();
        if (request.getAssignedTeammateNames() != null) {
            for (String teammateName : request.getAssignedTeammateNames()) {
                Teammate teammate = teammateRepository.findByName(teammateName)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate not found with name: " + teammateName));
                newAssignedNames.add(teammate.getName());
            }
            existingTask.setAssignedTeammateNames(String.join(ASSIGNED_NAMES_DELIMITER, newAssignedNames));
        } else {
            existingTask.setAssignedTeammateNames(""); // Clear assignments if null list is sent
        }


        Task updatedTask = taskRepository.save(existingTask); // Save task first to ensure updated assignments are persisted

        // Collect all unique teammate names involved in this task (old and new assignments)
        Set<String> allAffectedTeammates = new HashSet<>(oldAssignedNames);
        allAffectedTeammates.addAll(newAssignedNames);

        // If the task completion status changed, or assignments changed, update availability for all affected teammates
        if (!Objects.equals(wasCompleted, updatedTask.getIsCompleted()) || !oldAssignedNames.equals(newAssignedNames)) {
            for (String teammateName : allAffectedTeammates) {
                updateTeammateAvailability(teammateName);
            }
        }

        return convertToDto(updatedTask);
    }

    // Helper to update a teammate's availability based on all their active tasks
    // This is crucial because availability is now derived from the 'tasks' table directly.
    private void updateTeammateAvailability(String teammateName) {
        teammateRepository.findByName(teammateName).ifPresent(teammate -> {
            // A teammate is 'Occupied' if they are assigned to any NON-COMPLETED task.
            boolean isOccupied = taskRepository.findAll().stream()
                    .filter(task -> !task.getIsCompleted()) // Only consider non-completed tasks
                    .anyMatch(task -> {
                        if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                            return false;
                        }
                        return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                                .map(String::trim)
                                .anyMatch(name -> name.equalsIgnoreCase(teammate.getName()));
                    });

            String newStatus = isOccupied ? "Occupied" : "Free";
            if (!teammate.getAvailabilityStatus().equals(newStatus)) {
                teammate.setAvailabilityStatus(newStatus);
                teammateRepository.save(teammate);
            }
        });
    }

    // Delete a task
    @Transactional
    public void deleteTask(Long id) {
        Task taskToDelete = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + id));

        // Get assigned teammates from the task to be deleted
        Set<String> affectedTeammates = new HashSet<>();
        if (taskToDelete.getAssignedTeammateNames() != null && !taskToDelete.getAssignedTeammateNames().isEmpty()) {
            affectedTeammates.addAll(Arrays.stream(taskToDelete.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        taskRepository.delete(taskToDelete);

        // Update availability of affected teammates after deletion
        for (String teammateName : affectedTeammates) {
            updateTeammateAvailability(teammateName);
        }
    }

    // New method for searching tasks by name
    public List<TaskResponse> searchTasks(String taskName) {
        return taskRepository.findByTaskNameContainingIgnoreCase(taskName).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
