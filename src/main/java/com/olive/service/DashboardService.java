package com.olive.service;

import com.olive.dto.DashboardSummaryResponse;
import com.olive.dto.DashboardTaskDTO;
import com.olive.dto.DashboardTeammateDTO;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.security.UserDetailsImpl;
import org.slf4j.Logger;
import com.olive.model.Project;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final TaskRepository taskRepository;
    private final TeammateRepository teammateRepository;
    private final ProjectRepository projectRepository;

    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public DashboardService(TaskRepository taskRepository, TeammateRepository teammateRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
        this.projectRepository = projectRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {
        logger.info("Generating dashboard summary.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Teammate> teammatesForSummary;
        List<Task> tasksForSummary;
        List<Long> userProjectIds = userDetails.getProjectIds(); // This can be multiple
        String userRole = userDetails.getRole();

        // Determine scope of data based on user role and project ID(s)
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            logger.info("User is ADMIN, fetching global data for dashboard summary.");
            teammatesForSummary = teammateRepository.findAll();
            tasksForSummary = taskRepository.findAll();
        } else if (userProjectIds != null && !userProjectIds.isEmpty()) {
            logger.info("User is {} from project IDs {}. Fetching project-specific data for dashboard summary.", userRole, userProjectIds);
            teammatesForSummary = teammateRepository.findByProjectIdIn(userProjectIds); // Fetch teammates across all assigned projects
            tasksForSummary = taskRepository.findByProjectIdIn(userProjectIds); // Fetch tasks across all assigned projects
        } else {
            logger.warn("User {} with role {} has no projectIds assigned. Dashboard summary will be empty.", userDetails.getEmail(), userRole);
            teammatesForSummary = Collections.emptyList();
            tasksForSummary = Collections.emptyList();
        }

        logger.debug("Fetched {} teammates and {} tasks for dashboard summary based on user scope.", teammatesForSummary.size(), tasksForSummary.size());

        long totalTeammates = teammatesForSummary.size();
        long freeTeammates = teammatesForSummary.stream()
                .filter(t -> "Free".equals(t.getAvailabilityStatus()))
                .count();
        long occupiedTeammates = teammatesForSummary.stream()
                .filter(t -> "Occupied".equals(t.getAvailabilityStatus()))
                .count();

        long totalTasks = tasksForSummary.size();
        long activeTasks = tasksForSummary.stream()
                .filter(task -> !task.getIsCompleted() && !task.getCurrentStage().equalsIgnoreCase("Prod"))
                .count();
        logger.debug("Calculated totalTasks: {}, activeTasks: {}", totalTasks, activeTasks);


        // Tasks by stage: now using repository query for single or multiple projects
        Map<String, Long> tasksByStage;
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            tasksByStage = tasksForSummary.stream() // If ADMIN, use already fetched global tasks
                    .collect(Collectors.groupingBy(Task::getCurrentStage, Collectors.counting()));
        } else if (userProjectIds != null && !userProjectIds.isEmpty()) {
            tasksByStage = taskRepository.countTasksByStageAndProjectIdIn(userProjectIds).stream() // Use project-specific query for multiple projects
                    .collect(Collectors.toMap(
                            array -> (String) array[0],
                            array -> (Long) array[1]
                    ));
        } else {
            tasksByStage = Collections.emptyMap();
        }
        logger.debug("Tasks by stage: {}", tasksByStage);

        // Tasks by issue type: now using repository query for single or multiple projects
        Map<String, Long> tasksByIssueType;
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            tasksByIssueType = tasksForSummary.stream() // If ADMIN, use already fetched global tasks
                    .collect(Collectors.groupingBy(Task::getIssueType, Collectors.counting()));
        } else if (userProjectIds != null && !userProjectIds.isEmpty()) {
            tasksByIssueType = taskRepository.countTasksByIssueTypeAndProjectIdIn(userProjectIds).stream() // Use project-specific query for multiple projects
                    .collect(Collectors.toMap(
                            array -> (String) array[0],
                            array -> (Long) array[1]
                    ));
        } else {
            tasksByIssueType = Collections.emptyMap();
        }
        logger.debug("Tasks by issue type: {}", tasksByIssueType);


        long tasksPendingCodeReview;
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            tasksPendingCodeReview = taskRepository.countByIsCodeReviewDoneFalse(); // Global count for admin
        } else if (userProjectIds != null && !userProjectIds.isEmpty()) {
            tasksPendingCodeReview = taskRepository.countByProjectIdInAndIsCodeReviewDoneFalse(userProjectIds); // Multi-project count
        } else {
            tasksPendingCodeReview = 0;
        }
        logger.debug("Tasks pending code review: {}", tasksPendingCodeReview);


        long tasksPendingCmcApproval;
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            tasksPendingCmcApproval = taskRepository.countByIsCmcDoneFalse(); // Global count for admin
        } else if (userProjectIds != null && !userProjectIds.isEmpty()) {
            tasksPendingCmcApproval = taskRepository.countByProjectIdInAndIsCmcDoneFalse(userProjectIds); // Multi-project count
        } else {
            tasksPendingCmcApproval = 0;
        }
        logger.debug("Tasks pending CMC approval: {}", tasksPendingCmcApproval);


        // Recent tasks (e.g., top 5 most recently started, not completed, for the user's scope)
        logger.debug("Generating recent tasks list.");
        List<DashboardTaskDTO> recentTasks;
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            recentTasks = taskRepository.findTop10ByIsCompletedFalseOrderByStartDateDesc().stream()
                    .map(this::convertTaskToDashboardTaskDTO)
                    .collect(Collectors.toList());
        } else if (userProjectIds != null && !userProjectIds.isEmpty()) {
            recentTasks = taskRepository.findTop10ByProjectIdInAndIsCompletedFalseOrderByStartDateDesc(userProjectIds).stream()
                    .map(this::convertTaskToDashboardTaskDTO)
                    .collect(Collectors.toList());
        } else {
            recentTasks = Collections.emptyList();
        }
        logger.debug("Generated {} recent tasks: {}", recentTasks.size(), recentTasks.stream().map(DashboardTaskDTO::getName).collect(Collectors.joining(", ")));


        // Active tasks list for dashboard (not completed, not in Prod)
        logger.debug("Generating active tasks list.");
        List<DashboardTaskDTO> activeTasksList = tasksForSummary.stream() // Use tasksForSummary (already scoped)
                .filter(task -> !task.getIsCompleted() && !task.getCurrentStage().equalsIgnoreCase("Prod"))
                .map(this::convertTaskToDashboardTaskDTO)
                .collect(Collectors.toList());
        logger.debug("Generated {} active tasks list: {}", activeTasksList.size(), activeTasksList.stream().map(DashboardTaskDTO::getName).collect(Collectors.joining(", ")));


        // Team Members Summary (only for the user's scope)
        logger.debug("Generating team members summary.");
        List<DashboardTeammateDTO> teamMembersSummary = teammatesForSummary.stream() // Use teammatesForSummary (already scoped)
                .map(teammate -> {
                    long tasksAssignedToTeammate = tasksForSummary.stream() // Filter from tasks within user's scope
                            .filter(task -> !task.getIsCompleted()) // Only count active tasks
                            .filter(task -> {
                                if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                                    return false;
                                }
                                return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                                        .map(String::trim)
                                        .anyMatch(name -> name.equalsIgnoreCase(teammate.getName()));
                            })
                            .count();
                    // Fetch project name for the teammate's project (should be the same as user's project if non-admin)
                    String teammateProjectName = projectRepository.findById(teammate.getProjectId())
                            .map(Project::getProjectName)
                            .orElse("Unknown Project");

                    DashboardTeammateDTO teammateDTO = new DashboardTeammateDTO(
                            teammate.getTeammateId(),
                            teammate.getName(),
                            teammate.getRole(),
                            teammate.getEmail(),
                            teammate.getPhone(),
                            teammate.getDepartment(),
                            teammate.getLocation(),
                            tasksAssignedToTeammate,
                            teammate.getProjectId(),
                            teammateProjectName
                    );
                    logger.debug("Created DashboardTeammateDTO for '{}': tasksAssigned={}, ProjectName={}", teammate.getName(), tasksAssignedToTeammate, teammateProjectName);
                    return teammateDTO;
                })
                .collect(Collectors.toList());
        logger.debug("Team members summary generated for {} members.", teamMembersSummary.size());


        DashboardSummaryResponse response = new DashboardSummaryResponse(
                totalTeammates,
                freeTeammates,
                occupiedTeammates,
                totalTasks,
                activeTasks,
                tasksByStage,
                tasksByIssueType,
                tasksPendingCodeReview,
                tasksPendingCmcApproval,
                recentTasks,
                teamMembersSummary,
                activeTasksList
        );
        logger.info("Dashboard summary generated successfully.");
        return response;
    }

    // Helper to convert Task Entity to DashboardTaskDTO (now includes project name)
    private DashboardTaskDTO convertTaskToDashboardTaskDTO(Task task) {
        logger.debug("Converting Task '{}' (ID: {}) to DashboardTaskDTO.", task.getTaskName(), task.getTaskId());
        String assignee = null;
        if (task.getAssignedTeammateNames() != null && !task.getAssignedTeammateNames().isEmpty()) {
            assignee = Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .findFirst()
                    .orElse(null);
            logger.debug("Assigned teammate extracted: {}", assignee);
        }

        String formattedTaskNumber = null;
        if (task.getSequenceNumber() != null) {
            formattedTaskNumber = String.format("TSK-%03d", task.getSequenceNumber());
            logger.debug("Formatted task number: {}", formattedTaskNumber);
        }

        // Fetch project name for the task
        String projectName = projectRepository.findById(task.getProjectId())
                .map(Project::getProjectName)
                .orElse("Unknown Project");

        DashboardTaskDTO dto = new DashboardTaskDTO(
                task.getTaskId(),
                task.getTaskName(),
                task.getCurrentStage(),
                assignee,
                task.getDueDate(),
                task.getPriority(),
                formattedTaskNumber,
                task.getProjectId(),
                projectName
        );
        logger.debug("Finished converting Task to DashboardTaskDTO: ID={}, Name={}, TaskNumber={}, ProjectName={}", dto.getId(), dto.getName(), dto.getTaskNumber(), dto.getProjectName());
        return dto;
    }
}
