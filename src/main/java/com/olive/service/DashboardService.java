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
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TaskService taskService;

    @Autowired
    public DashboardService(TaskRepository taskRepository, TeammateRepository teammateRepository, TaskService taskService) {
        this.taskRepository = taskRepository;
        this.teammateRepository = teammateRepository;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Long> userProjectIds = userDetails.getProjectIds();
        String functionalGroup = userDetails.getFunctionalGroup();

        if (userProjectIds == null || userProjectIds.isEmpty()) {
            return new DashboardSummaryResponse(0, 0, 0, 0, 0, Collections.emptyMap(), Collections.emptyMap(), 0, 0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<String> relevantGroups = getRelevantGroupsForView(functionalGroup);
        Sort sort = Sort.by(Sort.Direction.ASC, "user.fullName");
        List<Teammate> teammatesForSummary = relevantGroups.isEmpty()
                ? Collections.emptyList()
                : teammateRepository.findByProjects_IdInAndUser_Role_FunctionalGroupIn(userProjectIds, relevantGroups, sort);

        List<Task> allTasksInProjects = taskRepository.findByProjectIdIn(userProjectIds);
        allTasksInProjects.forEach(task -> {
            Hibernate.initialize(task.getAssignedDevelopers());
            Hibernate.initialize(task.getAssignedTesters());
        });

        List<Task> tasksForSummary = allTasksInProjects.stream()
                .filter(task -> taskService.isTaskVisibleToRole(task, userDetails))
                .collect(Collectors.toList());

        long totalTeammates = teammatesForSummary.size();
        long freeTeammates = teammatesForSummary.stream().filter(t -> "Free".equals(t.getAvailabilityStatus())).count();
        long occupiedTeammates = totalTeammates - freeTeammates;

        long totalTasks = tasksForSummary.size();
        long activeTasks = tasksForSummary.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED)
                .count();

        Map<String, Long> tasksByStage = tasksForSummary.stream().collect(Collectors.groupingBy(task -> task.getStatus().getDisplayName(), Collectors.counting()));
        Map<TaskType, Long> tasksByTaskType = tasksForSummary.stream().filter(task -> task.getTaskType() != null).collect(Collectors.groupingBy(Task::getTaskType, Collectors.counting()));

        List<DashboardTaskDTO> recentTasks = tasksForSummary.stream().limit(5).map(this::convertTaskToDashboardTaskDTO).collect(Collectors.toList());
        List<DashboardTaskDTO> activeTasksList = tasksForSummary.stream().filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED).map(this::convertTaskToDashboardTaskDTO).collect(Collectors.toList());
        List<DashboardTeammateDTO> teamMembersSummary = teammatesForSummary.stream().map(this::convertTeammateToDashboardTeammateDTO).collect(Collectors.toList());

        return new DashboardSummaryResponse(totalTeammates, freeTeammates, occupiedTeammates, totalTasks, activeTasks, tasksByStage, tasksByTaskType, 0, 0, recentTasks, teamMembersSummary, activeTasksList);
    }

    private List<String> getRelevantGroupsForView(String functionalGroup) {
        if ("ADMIN".equals(functionalGroup)) {
            return Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST", "MANAGER", "DEV_MANAGER", "TEST_MANAGER");
        }
        switch (functionalGroup) {
            case "DEV_MANAGER":
            case "DEV_LEAD":
                return Arrays.asList("DEVELOPER", "DEV_LEAD", "DEV_MANAGER");
            case "TEST_MANAGER":
            case "TEST_LEAD":
                return Arrays.asList("TESTER", "TEST_LEAD", "TEST_MANAGER");
            case "BUSINESS_ANALYST":
                return Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST", "MANAGER", "DEV_MANAGER", "TEST_MANAGER");
            case "MANAGER":
                return Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST", "MANAGER", "DEV_MANAGER", "TEST_MANAGER");
            default:
                return Collections.emptyList();
        }
    }

    private DashboardTaskDTO convertTaskToDashboardTaskDTO(Task task) {
        String developerName = task.getAssignedDevelopers().stream()
                .map(teammate -> teammate.getUser().getFullName())
                .findFirst()
                .orElse(null);
        String testerName = task.getAssignedTesters().stream()
                .map(teammate -> teammate.getUser().getFullName())
                .findFirst()
                .orElse(null);

        String formattedTaskNumber = "TSK-" + task.getSequenceNumber();
        String projectName = task.getProject() != null ? task.getProject().getProjectName() : "Unknown Project";

        return new DashboardTaskDTO(
                task.getTaskId(),
                task.getTaskName(),
                task.getStatus().getDisplayName(),
                task.getPriority(),
                formattedTaskNumber,
                task.getProject() != null ? task.getProject().getProjectId() : null,
                projectName,
                developerName,
                testerName,
                task.getDevelopmentDueHours(),
                task.getTestingDueHours()
        );
    }

    private DashboardTeammateDTO convertTeammateToDashboardTeammateDTO(Teammate teammate) {
        // FIX: Use the new repository method to correctly count assigned tasks.
        long tasksAssignedToTeammate = taskRepository.countTasksByTeammate(teammate);

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