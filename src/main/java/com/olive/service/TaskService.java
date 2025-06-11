package com.olive.service;

import com.olive.dto.TaskCreateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TaskUpdateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.model.Task;
import com.olive.model.TaskAssignment;
import com.olive.model.TaskStage;
import com.olive.model.Teammate;
import com.olive.repository.TaskAssignmentRepository;
import com.olive.repository.TaskRepository;
import com.olive.repository.TaskStageRepository;
import com.olive.repository.TeammateRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskStageRepository taskStageRepository;
    private final TeammateRepository teammateRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskStageRepository taskStageRepository,
                       TeammateRepository teammateRepository, TaskAssignmentRepository taskAssignmentRepository) {
        this.taskRepository = taskRepository;
        this.taskStageRepository = taskStageRepository;
        this.teammateRepository = teammateRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    // Helper to convert Task Entity to TaskResponse DTO
    private TaskResponse convertToDto(Task task) {
        List<TeammateResponse> assignedTeammates = taskAssignmentRepository.findByTask(task).stream()
                .filter(TaskAssignment::getIsActive) // Only active assignments
                .map(assignment -> new TeammateResponse(
                        assignment.getTeammate().getTeammateId(),
                        assignment.getTeammate().getName(),
                        assignment.getTeammate().getEmail(),
                        assignment.getTeammate().getAvailabilityStatus()
                ))
                .collect(Collectors.toList());

        return new TaskResponse(
                task.getTaskId(),
                task.getTaskName(),
                task.getDescription(),
                task.getCurrentStage().getStageName(),
                task.getStartDate(),
                task.getDueDate(),
                task.getIsCompleted(),
                task.getIssueType(),
                task.getReceivedDate(),
                task.getDevelopmentStartDate(),
                task.getIsCodeReviewDone(),
                task.getIsCmcDone(),
                assignedTeammates
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
        // Validate Task Stage
        TaskStage currentStage = taskStageRepository.findByStageName(request.getCurrentStageName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task Stage: " + request.getCurrentStageName()));

        // Create Task entity
        Task task = new Task();
        task.setTaskName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setCurrentStage(currentStage);
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setIssueType(request.getIssueType());
        task.setReceivedDate(request.getReceivedDate());
        task.setDevelopmentStartDate(request.getDevelopmentStartDate());
        // Default booleans are handled by entity definition

        Task savedTask = taskRepository.save(task);

        // Handle assignments if provided
        if (request.getAssignedTeammateIds() != null && !request.getAssignedTeammateIds().isEmpty()) {
            for (Long teammateId : request.getAssignedTeammateIds()) {
                assignTeammateToTask(savedTask.getTaskId(), teammateId);
            }
        }

        return convertToDto(savedTask);
    }

    // Update an existing task
    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + id));

        // Update fields if provided
        Optional.ofNullable(request.getTaskName()).ifPresent(existingTask::setTaskName);
        Optional.ofNullable(request.getDescription()).ifPresent(existingTask::setDescription);

        if (request.getCurrentStageName() != null) {
            TaskStage newStage = taskStageRepository.findByStageName(request.getCurrentStageName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task Stage: " + request.getCurrentStageName()));
            existingTask.setCurrentStage(newStage);
        }

        Optional.ofNullable(request.getStartDate()).ifPresent(existingTask::setStartDate);
        Optional.ofNullable(request.getDueDate()).ifPresent(existingTask::setDueDate);
        Optional.ofNullable(request.getIsCompleted()).ifPresent(existingTask::setIsCompleted);
        Optional.ofNullable(request.getIssueType()).ifPresent(existingTask::setIssueType);
        Optional.ofNullable(request.getReceivedDate()).ifPresent(existingTask::setReceivedDate);
        Optional.ofNullable(request.getDevelopmentStartDate()).ifPresent(existingTask::setDevelopmentStartDate);
        Optional.ofNullable(request.getIsCodeReviewDone()).ifPresent(existingTask::setIsCodeReviewDone);
        Optional.ofNullable(request.getIsCmcDone()).ifPresent(existingTask::setIsCmcDone);

        // Handle re-assignment: Simplistic approach - remove all active and re-add provided ones.
        // For production, you'd compare lists and only add/remove differences.
        if (request.getAssignedTeammateIds() != null) {
            // Deactivate all current assignments for this task
            taskAssignmentRepository.findByTask(existingTask).forEach(assignment -> {
                assignment.setIsActive(false);
                taskAssignmentRepository.save(assignment);
                // Also update teammate availability back to 'Free' if they have no other active tasks
                updateTeammateAvailabilityIfFree(assignment.getTeammate().getTeammateId());
            });

            // Create new assignments
            for (Long teammateId : request.getAssignedTeammateIds()) {
                assignTeammateToTask(id, teammateId); // This method will create new or reactivate existing
            }
        }

        return convertToDto(taskRepository.save(existingTask));
    }

    // Assign a teammate to a task
    @Transactional
    public TaskResponse assignTeammateToTask(Long taskId, Long teammateId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));
        Teammate teammate = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + teammateId));

        // Check if an active assignment already exists
        Optional<TaskAssignment> existingActiveAssignment = taskAssignmentRepository.findByTaskAndTeammate(task, teammate)
                .filter(TaskAssignment::getIsActive);

        if (existingActiveAssignment.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate is already actively assigned to this task.");
        }

        // Check for existing inactive assignment to reactivate
        Optional<TaskAssignment> existingInactiveAssignment = taskAssignmentRepository.findByTaskAndTeammate(task, teammate)
                .filter(assignment -> !assignment.getIsActive());

        TaskAssignment assignment;
        if (existingInactiveAssignment.isPresent()) {
            assignment = existingInactiveAssignment.get();
            assignment.setIsActive(true);
            assignment.setAssignedDate(LocalDate.now()); // Update assigned date on reactivation
        } else {
            assignment = new TaskAssignment(task, teammate);
        }

        taskAssignmentRepository.save(assignment);

        // Update teammate status to Occupied
        teammate.setAvailabilityStatus("Occupied");
        teammateRepository.save(teammate);

        return convertToDto(task);
    }

    // Unassign a teammate from a task (sets assignment to inactive)
    @Transactional
    public TaskResponse unassignTeammateFromTask(Long taskId, Long teammateId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));
        Teammate teammate = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + teammateId));

        TaskAssignment assignment = taskAssignmentRepository.findByTaskAndTeammate(task, teammate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found for task " + taskId + " and teammate " + teammateId));

        if (!assignment.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate is not actively assigned to this task.");
        }

        assignment.setIsActive(false);
        taskAssignmentRepository.save(assignment);

        // Update teammate availability back to 'Free' if they have no other active tasks
        updateTeammateAvailabilityIfFree(teammateId);

        return convertToDto(task);
    }

    // Helper to set teammate to 'Free' if they have no other active assignments
    private void updateTeammateAvailabilityIfFree(Long teammateId) {
        Teammate teammate = teammateRepository.findById(teammateId)
                .orElse(null); // Should not be null if called from unassign

        if (teammate != null) {
            long activeAssignmentsCount = taskAssignmentRepository.findByTeammate(teammate).stream()
                    .filter(TaskAssignment::getIsActive)
                    .count();

            if (activeAssignmentsCount == 0) {
                teammate.setAvailabilityStatus("Free");
                teammateRepository.save(teammate);
            }
        }
    }

    // Delete a task (also removes associated assignments)
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + id));

        // Deactivate associated assignments and update teammate statuses
        taskAssignmentRepository.findByTask(task).forEach(assignment -> {
            assignment.setIsActive(false);
            taskAssignmentRepository.save(assignment);
            updateTeammateAvailabilityIfFree(assignment.getTeammate().getTeammateId());
        });

        taskRepository.delete(task);
    }
}
