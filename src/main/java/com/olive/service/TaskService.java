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
import org.springframework.util.StringUtils;
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

    @Transactional(readOnly = true)
    public TasksSummaryResponse getAllTasks(String taskNameFilter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Long> userProjectIds = userDetails.getProjectIds();

        List<Task> tasksToReturn;

        if ("ADMIN".equalsIgnoreCase(userDetails.getFunctionalGroup()) || "HR".equalsIgnoreCase(userDetails.getFunctionalGroup())) {
            tasksToReturn = taskRepository.findAll();
        }
        else if (userProjectIds != null && !userProjectIds.isEmpty()) {
            List<Task> tasksInUserProjects = taskRepository.findByProjectIdIn(userProjectIds);

            tasksToReturn = tasksInUserProjects.stream()
                    .filter(task -> isTaskVisibleToRole(task, userDetails))
                    .collect(Collectors.toList());

            if (taskNameFilter != null && !taskNameFilter.trim().isEmpty()) {
                tasksToReturn = tasksToReturn.stream()
                        .filter(task -> task.getTaskName().toLowerCase().contains(taskNameFilter.toLowerCase()))
                        .collect(Collectors.toList());
            }
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
        task.setSequenceNumber(generateNextSequenceNumber(parentTask));
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
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
        String functionalGroup = userDetails.getFunctionalGroup();

        authorizeAccessOrThrow(existingTask.getProject().getProjectId(), "You can only update tasks within your assigned projects.");

        validateTaskStatusTransition(existingTask.getStatus(), request.getStatus(), functionalGroup, request.getComment());
        validateMandatoryFieldsForStatus(request.getStatus(), request.getCommitId());

        if (StringUtils.hasText(request.getComment())) {
            taskActivityService.addComment(existingTask, currentUser, request.getComment());
        }

        Set<Teammate> oldAssignedTeammates = new HashSet<>(existingTask.getAssignedTeammates());

        logFieldChange(existingTask, currentUser, "status", existingTask.getStatus(), request.getStatus());
        logFieldChange(existingTask, currentUser, "taskName", existingTask.getTaskName(), request.getTaskName());
        logFieldChange(existingTask, currentUser, "description", existingTask.getDescription(), request.getDescription());
        logFieldChange(existingTask, currentUser, "priority", existingTask.getPriority(), request.getPriority());
        logFieldChange(existingTask, currentUser, "dueDate", existingTask.getDueDate(), request.getDueDate());
        logFieldChange(existingTask, currentUser, "taskType", existingTask.getTaskType(), request.getTaskType());

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

    public String generateNextSequenceNumber(Task parentTask) {
        if (parentTask != null) {
            long subTaskCount = taskRepository.countByParentTask(parentTask);
            return parentTask.getSequenceNumber() + "." + (subTaskCount + 1);
        } else {
            List<String> parentNumbers = taskRepository.findAllParentSequenceNumbers();
            int maxParentNum = parentNumbers.stream()
                    .filter(s -> !s.contains("."))
                    .mapToInt(s -> {
                        try { return Integer.parseInt(s); }
                        catch (NumberFormatException e) { return 0; }
                    })
                    .max().orElse(0);
            return String.valueOf(maxParentNum + 1);
        }
    }

    public boolean isTaskVisibleToRole(Task task, UserDetailsImpl userDetails) {
        String functionalGroup = userDetails.getFunctionalGroup();

        switch (functionalGroup) {
            case "DEVELOPER":
            case "DEV_LEAD":
                return task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED;

            case "TESTER":
            case "TEST_LEAD":
                User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
                if (currentUser != null) {
                    Teammate currentTeammate = teammateRepository.findByUser(currentUser).orElse(null);
                    return currentTeammate != null && task.getAssignedTeammates().contains(currentTeammate);
                }
                return false;

            case "MANAGER":
            case "DEV_MANAGER":
            case "TEST_MANAGER":
            case "BUSINESS_ANALYST":
                return true;
            default:
                return false;
        }
    }

    private void validateTaskStatusTransition(TaskStatus oldStatus, TaskStatus newStatus, String functionalGroup, String comment) {
        if (oldStatus == newStatus) return;

        if (COMPLETED_OR_CLOSED_STATUSES.contains(oldStatus) && newStatus != TaskStatus.REOPENED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A " + oldStatus.getDisplayName() + " task can only be Reopened.");
        }

        if ((oldStatus == TaskStatus.CODE_REVIEW) ||
                (oldStatus == TaskStatus.UAT_TESTING && newStatus == TaskStatus.UAT_FAILED) ||
                (oldStatus == TaskStatus.UAT_FAILED && newStatus == TaskStatus.UAT_TESTING)) {
            if (!StringUtils.hasText(comment)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A comment is required for this status change.");
            }
        }

        switch (newStatus) {
            case CODE_REVIEW:
                if (!"DEVELOPER".equals(functionalGroup)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a DEVELOPER can move a task to Code Review.");
                break;
            case UAT_TESTING:
                if (oldStatus == TaskStatus.CODE_REVIEW) {
                    if (!Arrays.asList("MANAGER", "DEV_LEAD", "BUSINESS_ANALYST",", TEST_LEAD","DEV_MANAGER").contains(functionalGroup))
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Manager, Dev Lead, or BA can approve a Code Review.");
                } else if (oldStatus == TaskStatus.UAT_FAILED) {
                    if (!("DEVELOPER".equals(functionalGroup) || "DEV_LEAD".equals(functionalGroup)))
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a DEVELOPER can move a task back to UAT Testing after a fix.");
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transition to UAT Testing.");
                }
                break;
            case UAT_FAILED:
                if (oldStatus != TaskStatus.UAT_TESTING) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task must be in UAT Testing to be marked as failed.");
                if (!"TESTER".equals(functionalGroup) && !"TEST_LEAD".equals(functionalGroup)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Tester or Test Lead can fail a UAT task.");
                break;
            case READY_FOR_PREPROD:
                if (oldStatus != TaskStatus.UAT_TESTING) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task must pass UAT Testing to be marked as Ready for Pre-Prod.");
                if (!"TESTER".equals(functionalGroup) && !"TEST_LEAD".equals(functionalGroup)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Tester or Test Lead can sign off on UAT.");
                break;
            case PREPROD:
                if (oldStatus != TaskStatus.READY_FOR_PREPROD) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task must be marked 'Ready for Pre-Prod' before deployment.");
                if (!"DEVELOPER".equals(functionalGroup) && !"DEV_LEAD".equals(functionalGroup)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Developer or Dev Lead can deploy to Pre-Prod.");
                break;
            case PROD:
                if (oldStatus != TaskStatus.PREPROD) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task must be in Pre-Production before moving to Production.");
                if (!"DEVELOPER".equals(functionalGroup) && !"DEV_LEAD".equals(functionalGroup)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Developer or Dev Lead can deploy to Production.");
                break;
        }
    }

    private void validateMandatoryFieldsForStatus(TaskStatus newStatus, String commitId) {
        if (newStatus == TaskStatus.UAT_TESTING || newStatus == TaskStatus.PREPROD) {
            if (!StringUtils.hasText(commitId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A commit ID is mandatory to move the task to " + newStatus.getDisplayName() + ".");
            }
        }
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
        if (!"ADMIN".equalsIgnoreCase(userDetails.getFunctionalGroup()) && !userDetails.getProjectIds().contains(projectId)) {
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate " + teammate.getUser().getFullName() + " is not assigned to project " + project.getProjectName() + ".");
            }
        }
        return teammates;
    }

    private void updateTeammateAvailabilityFor(Set<Teammate> teammates) {
        if (teammates != null) {
            teammates.forEach(teammate -> teammateService.updateTeammateAvailability(teammate.getUser().getFullName()));
        }
    }

    private TaskResponse convertToDto(Task task) {
        List<Long> assignedTeammateIds = task.getAssignedTeammates().stream().map(Teammate::getTeammateId).collect(Collectors.toList());
        List<String> assignedTeammateNames = task.getAssignedTeammates().stream().map(teammate -> teammate.getUser().getFullName()).collect(Collectors.toList());
        String formattedTaskNumber = task.getSequenceNumber();
        String projectName = task.getProject() != null ? task.getProject().getProjectName() : "Unknown Project";
        Long parentId = task.getParentTask() != null ? task.getParentTask().getTaskId() : null;
        String parentTaskTitle = task.getParentTask() != null ? task.getParentTask().getTaskName() : null;
        String parentTaskFormattedNumber = task.getParentTask() != null ? task.getParentTask().getSequenceNumber() : null;

        return new TaskResponse(
                task.getTaskId(), task.getTaskName(), formattedTaskNumber, task.getDescription(), task.getTaskType(), task.getStatus(),
                parentId, parentTaskTitle, parentTaskFormattedNumber, task.getReceivedDate(), task.getDevelopmentStartDate(), task.getDueDate(),
                task.getPriority(), assignedTeammateIds, assignedTeammateNames, task.getProject().getProjectId(), projectName, task.getDocumentPath(), task.getCommitId()
        );
    }
}
