package com.olive.service;

import com.olive.dto.DashboardSummaryResponse;
import com.olive.dto.DashboardTaskDTO;
import com.olive.dto.DashboardTeammateDTO;
import com.olive.model.Project;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.model.enums.TaskStatus;
import com.olive.model.enums.TaskType;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final TaskRepository taskRepository;
    private final TeammateRepository teammateRepository;

    @Autowired
    public DashboardService(TaskRepository taskRepository, TeammateRepository teammateRepository) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Long> userProjectIds = userDetails.getProjectIds();
        String functionalGroup = userDetails.getFunctionalGroup();

        if (userProjectIds == null || userProjectIds.isEmpty()) {
            return new DashboardSummaryResponse(0, 0, 0, 0, 0, Collections.emptyMap(), Collections.emptyMap(), 0, 0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<String> relevantGroups;
        switch (functionalGroup) {
            case "DEV_LEAD":
            case "MANAGER":
                relevantGroups = Arrays.asList("DEVELOPER", "DEV_LEAD");
                break;
            case "TEST_LEAD":
                relevantGroups = Arrays.asList("TESTER", "TEST_LEAD");
                break;
            case "BUSINESS_ANALYST":
                relevantGroups = Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST");
                break;
            default:
                relevantGroups = Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST", "MANAGER");
                break;
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "user.fullName");
        List<Teammate> teammatesForSummary = teammateRepository.findByProjects_IdInAndUser_Role_FunctionalGroupIn(userProjectIds, relevantGroups, sort);
        List<Task> tasksForSummary = taskRepository.findByProjectIdIn(userProjectIds);

        long totalTeammates = teammatesForSummary.size();
        long freeTeammates = teammatesForSummary.stream().filter(t -> "Free".equals(t.getAvailabilityStatus())).count();
        long occupiedTeammates = totalTeammates - freeTeammates;

        long totalTasks = tasksForSummary.size();
        long activeTasks = tasksForSummary.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED)
                .count();

        Map<String, Long> tasksByStage = tasksForSummary.stream()
                .collect(Collectors.groupingBy(task -> task.getStatus().getDisplayName(), Collectors.counting()));

        Map<TaskType, Long> tasksByTaskType = tasksForSummary.stream()
                .filter(task -> task.getTaskType() != null)
                .collect(Collectors.groupingBy(Task::getTaskType, Collectors.counting()));

        long tasksPendingCodeReview = 0;
        long tasksPendingCmcApproval = 0;

        List<DashboardTaskDTO> recentTasks = taskRepository.findTop5ByOrderByDevelopmentStartDateDesc().stream()
                .map(this::convertTaskToDashboardTaskDTO)
                .collect(Collectors.toList());

        List<DashboardTaskDTO> activeTasksList = tasksForSummary.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED)
                .map(this::convertTaskToDashboardTaskDTO)
                .collect(Collectors.toList());

        List<DashboardTeammateDTO> teamMembersSummary = teammatesForSummary.stream()
                .map(this::convertTeammateToDashboardTeammateDTO)
                .collect(Collectors.toList());

        return new DashboardSummaryResponse(
                totalTeammates, freeTeammates, occupiedTeammates, totalTasks, activeTasks,
                tasksByStage, tasksByTaskType, tasksPendingCodeReview, tasksPendingCmcApproval,
                recentTasks, teamMembersSummary, activeTasksList
        );
    }

    private DashboardTaskDTO convertTaskToDashboardTaskDTO(Task task) {
        String assignee = task.getAssignedTeammates().stream()
                .map(teammate -> teammate.getUser().getFullName())
                .findFirst()
                .orElse(null);
        String formattedTaskNumber = "TSK-" + task.getSequenceNumber();
        String projectName = task.getProject() != null ? task.getProject().getProjectName() : "Unknown Project";

        return new DashboardTaskDTO(
                task.getTaskId(), task.getTaskName(), task.getStatus().getDisplayName(), assignee,
                task.getDueDate(), task.getPriority(), formattedTaskNumber,
                task.getProject() != null ? task.getProject().getProjectId() : null, projectName
        );
    }

    private DashboardTeammateDTO convertTeammateToDashboardTeammateDTO(Teammate teammate) {
        long tasksAssignedToTeammate = teammate.getAssignedTasks().stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED)
                .count();
        List<Long> teammateProjectIdsList = teammate.getProjects().stream()
                .map(Project::getProjectId)
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> teammateProjectNames = teammate.getProjects().stream()
                .map(Project::getProjectName)
                .collect(Collectors.toList());

        return new DashboardTeammateDTO(
                teammate.getTeammateId(), teammate.getUser().getFullName(), teammate.getUser().getRole().getTitle(),
                teammate.getUser().getEmail(), teammate.getPhone(), teammate.getDepartment(), teammate.getLocation(),
                tasksAssignedToTeammate, teammateProjectIdsList, teammateProjectNames
        );
    }
}
