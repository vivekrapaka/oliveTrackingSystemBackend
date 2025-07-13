package com.olive.service;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
import com.olive.model.*;
import com.olive.model.enums.TaskStatus;
import com.olive.repository.ProjectRepository;
import com.olive.repository.RoleRepository;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.UserRepository;
import com.olive.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeammateService {

    private static final Logger logger = LoggerFactory.getLogger(TeammateService.class);

    private final TeammateRepository teammateRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public TeammateService(TeammateRepository teammateRepository, ProjectRepository projectRepository, UserRepository userRepository, RoleRepository roleRepository, TaskRepository taskRepository) {
        this.teammateRepository = teammateRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.taskRepository = taskRepository;
    }

    public TeammatesSummaryResponse getAllTeammatesSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Long> userProjectIds = userDetails.getProjectIds();
        String functionalGroup = userDetails.getFunctionalGroup();

        if (userProjectIds == null || userProjectIds.isEmpty()) {
            return new TeammatesSummaryResponse(0, 0, 0, 0, Collections.emptyList());
        }

        // FIX: Using the corrected filtering logic to ensure discipline segregation
        List<String> relevantGroups = getRelevantGroupsForView(functionalGroup);
        Sort sort = Sort.by(Sort.Direction.ASC, "user.fullName");
        List<Teammate> teammatesToConsider = relevantGroups.isEmpty()
                ? Collections.emptyList()
                : teammateRepository.findByProjects_IdInAndUser_Role_FunctionalGroupIn(userProjectIds, relevantGroups, sort);

        long totalMembers = teammatesToConsider.size();
        long availableMembers = teammatesToConsider.stream().filter(t -> "Free".equals(t.getAvailabilityStatus())).count();
        long occupiedMembers = totalMembers - availableMembers;

        List<TeammateResponse> teammateResponses = teammatesToConsider.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // The activeTasksCount is a global count for the dashboard, not relevant here.
        return new TeammatesSummaryResponse(totalMembers, availableMembers, occupiedMembers, 0, teammateResponses);
    }

    private List<String> getRelevantGroupsForView(String functionalGroup) {
        if ("ADMIN".equals(functionalGroup)) {
            return Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST", "MANAGER", "DEV_MANAGER", "TEST_MANAGER");
        }
        switch (functionalGroup) {
            case "DEV_MANAGER":
            case "DEV_LEAD":
            case "DEVELOPER": // A developer should see their own team
                return Arrays.asList("DEVELOPER", "DEV_LEAD", "DEV_MANAGER");
            case "TEST_MANAGER":
            case "TEST_LEAD":
            case "TESTER": // A tester should see their own team
                return Arrays.asList("TESTER", "TEST_LEAD", "TEST_MANAGER");
            case "BUSINESS_ANALYST":
                return Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST", "MANAGER", "DEV_MANAGER", "TEST_MANAGER");
            case "MANAGER":
                return Arrays.asList("DEVELOPER", "DEV_LEAD", "TESTER", "TEST_LEAD", "BUSINESS_ANALYST", "MANAGER", "DEV_MANAGER", "TEST_MANAGER");
            default:
                return Collections.emptyList(); // Default to an empty list for any other roles
        }
    }

    public TeammateResponse getTeammateById(Long id) {
        Teammate teammate = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));
        return convertToDto(teammate);
    }

    @Transactional
    public TeammateResponse updateTeammate(Long teammateId, TeammateCreateRequest request) {
        Teammate existingTeammate = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + teammateId));

        Role newRole = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Role ID."));

        User user = existingTeammate.getUser();
        user.setFullName(request.getFullName());
        user.setRole(newRole);
        userRepository.save(user);

        existingTeammate.setPhone(request.getPhone());
        existingTeammate.setDepartment(request.getDepartment());
        existingTeammate.setLocation(request.getLocation());

        if (request.getProjectIds() != null) {
            Set<Project> updatedProjects = new HashSet<>(projectRepository.findAllById(request.getProjectIds()));
            existingTeammate.setProjects(updatedProjects);
        }

        Teammate updatedTeammate = teammateRepository.save(existingTeammate);
        return convertToDto(updatedTeammate);
    }

    @Transactional
    public void deleteTeammate(Long teammateId) {
        Teammate teammateToDelete = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + teammateId));

        List<Task> assignedTasks = taskRepository.findTasksByTeammate(teammateToDelete);

        boolean isAssignedToActiveTask = assignedTasks.stream()
                .anyMatch(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED);

        if (isAssignedToActiveTask) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate is assigned to active tasks and cannot be deleted.");
        }

        for (Task task : assignedTasks) {
            task.getAssignedDevelopers().remove(teammateToDelete);
            task.getAssignedTesters().remove(teammateToDelete);
            taskRepository.save(task);
        }

        teammateRepository.delete(teammateToDelete);
    }

    @Transactional
    public void updateTeammateAvailability(String userFullName) {
        userRepository.findByFullName(userFullName).ifPresent(user -> {
            teammateRepository.findByUser(user).ifPresent(teammate -> {
                boolean isOccupied = calculateIsOccupied(teammate);
                String newStatus = isOccupied ? "Occupied" : "Free";
                if (!teammate.getAvailabilityStatus().equals(newStatus)) {
                    teammate.setAvailabilityStatus(newStatus);
                    teammateRepository.save(teammate);
                    logger.info("Teammate '{}' availability changed to: {}", teammate.getUser().getFullName(), newStatus);
                }
            });
        });
    }

    private boolean calculateIsOccupied(Teammate teammate) {
        List<Task> assignedTasks = taskRepository.findTasksByTeammate(teammate);
        return assignedTasks.stream()
                .anyMatch(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED);
    }

    private TeammateResponse convertToDto(Teammate teammate) {
        List<Long> projectIds = teammate.getProjects().stream().map(Project::getProjectId).collect(Collectors.toList());
        List<String> projectNames = teammate.getProjects().stream().map(Project::getProjectName).collect(Collectors.toList());

        long activeTasks = taskRepository.countTasksByTeammate(teammate);
        long completedTasks = 0; // This would require a more complex query, setting to 0 for now.

        User user = teammate.getUser();
        return new TeammateResponse(
                teammate.getTeammateId(), user.getFullName(), user.getEmail(), user.getRole().getTitle(),
                teammate.getPhone(), teammate.getDepartment(), teammate.getLocation(), teammate.getAvatar(),
                teammate.getAvailabilityStatus(), activeTasks, completedTasks, projectIds, projectNames
        );
    }
}
