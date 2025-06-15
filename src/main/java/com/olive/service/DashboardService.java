package com.olive.service;

import com.olive.dto.DashboardSummaryResponse;
import com.olive.dto.DashboardTaskDTO;
import com.olive.dto.DashboardTeammateDTO;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final TaskRepository taskRepository;
    private final TeammateRepository teammateRepository;

    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public DashboardService(TaskRepository taskRepository, TeammateRepository teammateRepository) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {
        logger.info("Generating dashboard summary.");

        List<Teammate> allTeammates = teammateRepository.findAll();
        List<Task> allTasks = taskRepository.findAll();
        logger.debug("Fetched {} teammates and {} tasks for dashboard summary.", allTeammates.size(), allTasks.size());

        long totalTeammates = allTeammates.size();
        long freeTeammates = allTeammates.stream()
                .filter(t -> "Free".equals(t.getAvailabilityStatus()))
                .count();
        long occupiedTeammates = allTeammates.stream()
                .filter(t -> "Occupied".equals(t.getAvailabilityStatus()))
                .count();

        long totalTasks = allTasks.size();
        // Active tasks are those not completed and not in "Prod" (assuming "Prod" means completed/deployed)
        long activeTasks = allTasks.stream()
                .filter(task -> !task.getIsCompleted() && !task.getCurrentStage().equalsIgnoreCase("Prod"))
                .count();
        logger.debug("Calculated totalTasks: {}, activeTasks: {}", totalTasks, activeTasks);


        Map<String, Long> tasksByStage = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getCurrentStage, Collectors.counting()));
        logger.debug("Tasks by stage: {}", tasksByStage);

        Map<String, Long> tasksByIssueType = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getIssueType, Collectors.counting()));
        logger.debug("Tasks by issue type: {}", tasksByIssueType);


        long tasksPendingCodeReview = allTasks.stream()
                .filter(task -> !task.getIsCodeReviewDone())
                .count();
        logger.debug("Tasks pending code review: {}", tasksPendingCodeReview);


        long tasksPendingCmcApproval = allTasks.stream()
                .filter(task -> !task.getIsCmcDone())
                .count();
        logger.debug("Tasks pending CMC approval: {}", tasksPendingCmcApproval);


        // Recent tasks (e.g., top 5 most recently started, not completed)
        logger.debug("Generating recent tasks list.");
        List<DashboardTaskDTO> recentTasks = taskRepository.findTop10ByIsCompletedFalseOrderByStartDateDesc().stream()
                .map(this::convertTaskToDashboardTaskDTO)
                .collect(Collectors.toList());
        logger.debug("Generated {} recent tasks: {}", recentTasks.size(), recentTasks.stream().map(DashboardTaskDTO::getName).collect(Collectors.joining(", ")));


        // Active tasks list for dashboard (not completed, not in Prod)
        logger.debug("Generating active tasks list.");
        List<DashboardTaskDTO> activeTasksList = allTasks.stream()
                .filter(task -> !task.getIsCompleted() && !task.getCurrentStage().equalsIgnoreCase("Prod"))
                .map(this::convertTaskToDashboardTaskDTO)
                .collect(Collectors.toList());
        logger.debug("Generated {} active tasks list: {}", activeTasksList.size(), activeTasksList.stream().map(DashboardTaskDTO::getName).collect(Collectors.joining(", ")));


        // Team Members Summary
        logger.debug("Generating team members summary.");
        List<DashboardTeammateDTO> teamMembersSummary = allTeammates.stream()
                .map(teammate -> {
                    long tasksAssignedToTeammate = allTasks.stream()
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
                    DashboardTeammateDTO teammateDTO = new DashboardTeammateDTO(
                            teammate.getTeammateId(),
                            teammate.getName(),
                            teammate.getRole(),
                            teammate.getEmail(),
                            teammate.getPhone(),
                            teammate.getDepartment(),
                            teammate.getLocation(),
                            tasksAssignedToTeammate
                    );
                    logger.debug("Created DashboardTeammateDTO for '{}': tasksAssigned={}", teammate.getName(), tasksAssignedToTeammate);
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

    // Helper to convert Task Entity to DashboardTaskDTO
    private DashboardTaskDTO convertTaskToDashboardTaskDTO(Task task) {
        logger.debug("Converting Task '{}' (ID: {}) to DashboardTaskDTO.", task.getTaskName(), task.getTaskId());
        String assignee = null;
        if (task.getAssignedTeammateNames() != null && !task.getAssignedTeammateNames().isEmpty()) {
            // Take the first assigned teammate as the primary assignee for display on dashboard
            assignee = Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                    .map(String::trim)
                    .findFirst()
                    .orElse(null);
            logger.debug("Assigned teammate extracted: {}", assignee);
        } else {
            logger.debug("No assigned teammates for task '{}'.", task.getTaskName());
        }

        // Format sequenceNumber to TSK-XXX string
        String formattedTaskNumber = null;
        if (task.getSequenceNumber() != null) {
            formattedTaskNumber = String.format("TSK-%03d", task.getSequenceNumber());
            logger.debug("Formatted task number: {}", formattedTaskNumber);
        } else {
            logger.warn("Task '{}' (ID: {}) has a null sequence number. Task number will be null.", task.getTaskName(), task.getTaskId());
        }

        DashboardTaskDTO dto = new DashboardTaskDTO(
                task.getTaskId(),
                task.getTaskName(),
                task.getCurrentStage(),
                assignee,
                task.getDueDate(),
                task.getPriority(),
                formattedTaskNumber // Populating the new taskNumber field
        );
        logger.debug("Finished converting Task to DashboardTaskDTO: ID={}, Name={}, TaskNumber={}", dto.getId(), dto.getName(), dto.getTaskNumber());
        return dto;
    }
}
