package com.olive.service;

import com.olive.dto.DashboardSummaryResponse;
import com.olive.dto.DashboardTaskDTO;
import com.olive.dto.DashboardTeammateDTO;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class DashboardService {
    private final TeammateRepository teammateRepository;
    private final TaskRepository taskRepository;
    private static final String ASSIGNED_NAMES_DELIMITER = ",";


    @Autowired
    public DashboardService(TeammateRepository teammateRepository, TaskRepository taskRepository) {
        this.teammateRepository = teammateRepository;
        this.taskRepository = taskRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {
        // Teammate Stats
        List<Teammate> allTeammates = teammateRepository.findAll();
        long totalTeammates = allTeammates.size();
        long freeTeammates = allTeammates.stream().filter(t -> "Free".equals(t.getAvailabilityStatus())).count();
        long occupiedTeammates = allTeammates.stream().filter(t -> "Occupied".equals(t.getAvailabilityStatus())).count();

        List<Task> allTasks = taskRepository.findAll();

        // Tasks by Stage
        Map<String, Long> tasksByStage = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getCurrentStage, Collectors.counting()));

        // Tasks by Issue Type
        Map<String, Long> tasksByIssueType = allTasks.stream()
                .filter(task -> task.getIssueType() != null && !task.getIssueType().isEmpty())
                .collect(Collectors.groupingBy(Task::getIssueType, Collectors.counting()));

        // Tasks pending code review/CMC (excluding completed tasks)
        long tasksPendingCodeReview = allTasks.stream()
                .filter(task -> !task.getIsCompleted() && !task.getIsCodeReviewDone())
                .count();
        long tasksPendingCmcApproval = allTasks.stream()
                .filter(task -> !task.getIsCompleted() && !task.getIsCmcDone())
                .count();

        // New: Recent Tasks (e.g., top 5 uncompleted tasks by start date descending)
        List<DashboardTaskDTO> recentTasks = taskRepository.findTop10ByIsCompletedFalseOrderByStartDateDesc()
                .stream()
                .map(task -> new DashboardTaskDTO(
                        task.getTaskId(),
                        task.getTaskName(),
                        task.getCurrentStage(),
                        task.getAssignedTeammateNames() != null && !task.getAssignedTeammateNames().isEmpty() ?
                                Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                                        .map(String::trim)
                                        .collect(Collectors.joining(", ")) : "Unassigned",
                        task.getDueDate(),
                        task.getPriority()
                ))
                .collect(Collectors.toList());

        // New: Team Members Summary
        List<DashboardTeammateDTO> teamMembersSummary = allTeammates.stream()
                .map(teammate -> {
                    long tasksAssignedCount = allTasks.stream()
                            .filter(task -> !task.getIsCompleted() && task.getAssignedTeammateNames() != null && !task.getAssignedTeammateNames().isEmpty())
                            .filter(task -> Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                                    .map(String::trim)
                                    .anyMatch(name -> name.equalsIgnoreCase(teammate.getName())))
                            .count();
                    return new DashboardTeammateDTO(
                            teammate.getTeammateId(),
                            teammate.getName(),
                            teammate.getRole(),
                            teammate.getEmail(),
                            teammate.getPhone(), // Include phone
                            teammate.getDepartment(), // Include department
                            teammate.getLocation(), // Include location
                            tasksAssignedCount
                    );
                })
                .sorted(Comparator.comparing(DashboardTeammateDTO::getName)) // Sort alphabetically
                .collect(Collectors.toList());


        return new DashboardSummaryResponse(
                totalTeammates,
                freeTeammates,
                occupiedTeammates,
                tasksByStage,
                tasksByIssueType,
                tasksPendingCodeReview,
                tasksPendingCmcApproval,
                recentTasks,
                teamMembersSummary
        );
    }
}
