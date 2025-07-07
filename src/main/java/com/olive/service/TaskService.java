package com.olive.service;

import com.olive.dto.TaskCreateUpdateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TasksSummaryResponse;
import com.olive.model.Project;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.model.User;
import com.olive.model.enums.TaskStatus;
import com.olive.model.enums.TaskType;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.UserRepository;
import com.olive.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final TeammateRepository teammateRepository;
    private final ProjectRepository projectRepository;
    private final TeammateService teammateService;
    private final TaskActivityService taskActivityService;
    private final UserRepository userRepository;

    private static final Set<TaskStatus> COMPLETED_OR_CLOSED_STATUSES = Set.of(TaskStatus.COMPLETED, TaskStatus.CLOSED);

    @Autowired
    public TaskService(TaskRepository taskRepository, TeammateRepository teammateRepository, ProjectRepository projectRepository, TeammateService teammateService, TaskActivityService taskActivityService, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
        this.projectRepository = projectRepository;
        this.teammateService = teammateService;
        this.taskActivityService = taskActivityService;
        this.userRepository = userRepository;
    }

    public TasksSummaryResponse getAllTasks(String taskNameFilter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Task> tasksToReturn;
        if ("ADMIN".equalsIgnoreCase(userDetails.getRole()) || "HR".equalsIgnoreCase(userDetails.getRole())) {
            tasksToReturn = (taskNameFilter != null && !taskNameFilter.trim().isEmpty()) ? taskRepository.findByTaskNameContainingIgnoreCase(taskNameFilter) : taskRepository.findAll();
        } else if (userDetails.getProjectIds() != null && !userDetails.getProjectIds().isEmpty()) {
            tasksToReturn = (taskNameFilter != null && !taskNameFilter.trim().isEmpty()) ? taskRepository.findByProjectIdInAndTaskNameContainingIgnoreCase(userDetails.getProjectIds(), taskNameFilter) : taskRepository.findByProjectIdIn(userDetails.getProjectIds());
        } else {
            tasksToReturn = Collections.emptyList();
        }
        List<TaskResponse> taskResponses = tasksToReturn.stream().map(this::convertToDto).collect(Collectors.toList());
        return new TasksSummaryResponse(taskResponses.size(), taskResponses);
    }

    @Transactional
    public TaskResponse createTask(TaskCreateUpdateRequest request) {
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found with ID: " + request.getProjectId()));
        authorizeAccessOrThrow(project.getProjectId(), "You can only create tasks within projects you are assigned to.");
        if (request.getTaskType() == TaskType.EPIC && request.getParentId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EPIC tasks cannot have a parent task.");
        }
        Task parentTask = null;
        if (request.getParentId() != null) {
            parentTask = taskRepository.findById(request.getParentId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent task not found with ID: " + request.getParentId()));
        }
        taskRepository.findByProjectIdAndTaskNameIgnoreCase(project.getProjectId(), request.getTaskName()).ifPresent(t -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task with this name already exists in this project.");
        });

        Task task = new Task();
        task.setTaskName(request.getTaskName());
        task.setSequenceNumber(generateNextSequenceNumber());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setStatus(request.getStatus());
        task.setReceivedDate(request.getReceivedDate());
        task.setDevelopmentStartDate(request.getDevelopmentStartDate());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setProject(project);
        task.setParentTask(parentTask);
        task.setCommitId(request.getCommitId());
        Set<Teammate> assignedTeammates = findAndValidateTeammates(request.getAssignedTeammateIds(), project);
        task.setAssignedTeammates(assignedTeammates);
        Task savedTask = taskRepository.save(task);
        updateTeammateAvailabilityFor(savedTask.getAssignedTeammates());
        return convertToDto(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskCreateUpdateRequest request) {
        Task existingTask = taskRepository.findById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));
        authorizeAccessOrThrow(existingTask.getProject().getProjectId(), "You can only update tasks within your assigned projects.");

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElse(null);

        Set<Teammate> oldAssignedTeammates = new HashSet<>(existingTask.getAssignedTeammates());
        validateTaskStatusTransition(existingTask.getStatus(), request.getStatus());

        // --- Start Logging Changes ---
        logFieldChange(existingTask, currentUser, "taskName", existingTask.getTaskName(), request.getTaskName());
        logFieldChange(existingTask, currentUser, "description", existingTask.getDescription(), request.getDescription());
        logFieldChange(existingTask, currentUser, "priority", existingTask.getPriority(), request.getPriority());
        logFieldChange(existingTask, currentUser, "dueDate", existingTask.getDueDate(), request.getDueDate());
        logFieldChange(existingTask, currentUser, "status", existingTask.getStatus(), request.getStatus());
        logFieldChange(existingTask, currentUser, "taskType", existingTask.getTaskType(), request.getTaskType());
        // --- End Logging Changes ---

        existingTask.setTaskName(request.getTaskName());
        existingTask.setDescription(request.getDescription());
        existingTask.setTaskType(request.getTaskType());
        existingTask.setStatus(request.getStatus());
        existingTask.setReceivedDate(request.getReceivedDate());
        existingTask.setDevelopmentStartDate(request.getDevelopmentStartDate());
        existingTask.setDueDate(request.getDueDate());
        existingTask.setPriority(request.getPriority());
        existingTask.setCommitId(request.getCommitId());

        Set<Teammate> newAssignedTeammates = findAndValidateTeammates(request.getAssignedTeammateIds(), existingTask.getProject());
        existingTask.setAssignedTeammates(newAssignedTeammates);

        Task updatedTask = taskRepository.save(existingTask);

        Set<Teammate> allAffectedTeammates = new HashSet<>(oldAssignedTeammates);
        allAffectedTeammates.addAll(newAssignedTeammates);
        updateTeammateAvailabilityFor(allAffectedTeammates);

        return convertToDto(updatedTask);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task taskToDelete = taskRepository.findById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));
        authorizeAccessOrThrow(taskToDelete.getProject().getProjectId(), "You can only delete tasks within your assigned projects.");
        Set<Teammate> affectedTeammates = new HashSet<>(taskToDelete.getAssignedTeammates());
        taskRepository.delete(taskToDelete);
        updateTeammateAvailabilityFor(affectedTeammates);
    }

    public Long generateNextSequenceNumber() {
        return taskRepository.findMaxSequenceNumber().map(max -> max + 1).orElse(1L);
    }

    private void logFieldChange(Task task, User user, String fieldName, Object oldValue, Object newValue) {
        if (newValue != null && !Objects.equals(oldValue, newValue)) {
            String oldValueStr = (oldValue instanceof Enum) ? ((Enum<?>) oldValue).name() : Objects.toString(oldValue, "");
            String newValueStr = (newValue instanceof Enum) ? ((Enum<?>) newValue).name() : Objects.toString(newValue, "");
            taskActivityService.logChange(task, user, fieldName, oldValueStr, newValueStr);
        }
    }

    private void authorizeAccessOrThrow(Long projectId, String errorMessage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (!"ADMIN".equalsIgnoreCase(userDetails.getRole()) && !userDetails.getProjectIds().contains(projectId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
        }
    }

    private Set<Teammate> findAndValidateTeammates(List<Long> teammateIds, Project project) {
        if (teammateIds == null || teammateIds.isEmpty()) return new HashSet<>();
        Set<Teammate> teammates = new HashSet<>(teammateRepository.findAllById(teammateIds));
        if (teammates.size() != teammateIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more assigned teammates not found.");
        }
        for (Teammate teammate : teammates) {
            if (teammate.getProjects().stream().noneMatch(p -> p.getProjectId().equals(project.getProjectId()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate " + teammate.getName() + " is not assigned to project " + project.getProjectName() + ".");
            }
        }
        return teammates;
    }

    private void updateTeammateAvailabilityFor(Set<Teammate> teammates) {
        if (teammates != null) {
            teammates.forEach(teammate -> teammateService.updateTeammateAvailability(teammate.getName()));
        }
    }

    private void validateTaskStatusTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) return;
        if (COMPLETED_OR_CLOSED_STATUSES.contains(oldStatus) && newStatus != TaskStatus.REOPENED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change status from " + oldStatus.getDisplayName() + " unless reopening the task.");
        }
    }

    private TaskResponse convertToDto(Task task) {
        List<Long> assignedTeammateIds = task.getAssignedTeammates().stream().map(Teammate::getTeammateId).collect(Collectors.toList());
        List<String> assignedTeammateNames = task.getAssignedTeammates().stream().map(Teammate::getName).collect(Collectors.toList());
        String formattedTaskNumber = String.format("TSK-%03d", task.getSequenceNumber());
        String projectName = task.getProject() != null ? task.getProject().getProjectName() : "Unknown Project";
        Long parentId = task.getParentTask() != null ? task.getParentTask().getTaskId() : null;
        String parentTaskTitle = task.getParentTask() != null ? task.getParentTask().getTaskName() : null;
        String parentTaskFormattedNumber = task.getParentTask() != null ? String.format("TSK-%03d", task.getParentTask().getSequenceNumber()) : null;

        return new TaskResponse(
                task.getTaskId(), task.getTaskName(), formattedTaskNumber, task.getDescription(), task.getTaskType(), task.getStatus(),
                parentId, parentTaskTitle, parentTaskFormattedNumber, task.getReceivedDate(), task.getDevelopmentStartDate(), task.getDueDate(),
                task.getPriority(), assignedTeammateIds, assignedTeammateNames, task.getProject().getProjectId(), projectName, task.getDocumentPath(), task.getCommitId()
        );
    }
}