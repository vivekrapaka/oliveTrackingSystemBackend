package com.olive.service;

import com.olive.dto.AssignmentSummaryDTO;
import com.olive.dto.DashboardSummaryResponse;
import com.olive.model.Task;
import com.olive.repository.TaskAssignmentRepository;
import com.olive.repository.TaskRepository;
import com.olive.repository.TaskStageRepository;
import com.olive.repository.TeammateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final TeammateRepository teammateRepository;
    private final TaskRepository taskRepository;
    private final TaskStageRepository taskStageRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    public DashboardService(TeammateRepository teammateRepository, TaskRepository taskRepository,
                            TaskStageRepository taskStageRepository, TaskAssignmentRepository taskAssignmentRepository) {
        this.teammateRepository = teammateRepository;
        this.taskRepository = taskRepository;
        this.taskStageRepository = taskStageRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {
        // Teammate Stats
        long totalTeammates = teammateRepository.count();
        long freeTeammates = teammateRepository.findByAvailabilityStatus("Free").size();
        long occupiedTeammates = teammateRepository.findByAvailabilityStatus("Occupied").size();

        // Tasks by Stage
        Map<String, Long> tasksByStage = taskRepository.findAll().stream()
                .collect(Collectors.groupingBy(task -> task.getCurrentStage().getStageName(), Collectors.counting()));

        // Tasks by Issue Type
        Map<String, Long> tasksByIssueType = taskRepository.findAll().stream()
                .filter(task -> task.getIssueType() != null && !task.getIssueType().isEmpty())
                .collect(Collectors.groupingBy(Task::getIssueType, Collectors.counting()));

        // Tasks pending code review/CMC (excluding completed tasks)
        long tasksPendingCodeReview = taskRepository.findAll().stream()
                .filter(task -> !task.getIsCompleted() && !task.getIsCodeReviewDone())
                .count();
        long tasksPendingCmcApproval = taskRepository.findAll().stream()
                .filter(task -> !task.getIsCompleted() && !task.getIsCmcDone())
                .count();

        // Active Assignments (for quick display)
        List<AssignmentSummaryDTO> activeAssignments = taskAssignmentRepository.findByIsActive(true).stream()
                .filter(assignment -> !assignment.getTask().getIsCompleted()) // Only show active, uncompleted assignments
                .map(assignment -> new AssignmentSummaryDTO(
                        assignment.getTask().getTaskId(),
                        assignment.getTask().getTaskName(),
                        assignment.getTeammate().getTeammateId(),
                        assignment.getTeammate().getName(),
                        assignment.getTask().getCurrentStage().getStageName()
                ))
                // Sort by task name or assigned date if desired
                .limit(10) // Limit for dashboard display
                .collect(Collectors.toList());


        return new DashboardSummaryResponse(
                totalTeammates,
                freeTeammates,
                occupiedTeammates,
                tasksByStage,
                tasksByIssueType,
                tasksPendingCodeReview,
                tasksPendingCmcApproval,
                activeAssignments
        );
    }
}
