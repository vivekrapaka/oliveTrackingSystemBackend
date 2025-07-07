package com.olive.service;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
import com.olive.model.Project;
import com.olive.model.Teammate;
import com.olive.model.User;
import com.olive.model.enums.TaskStatus;
import com.olive.repository.ProjectRepository;
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

    @Autowired
    public TeammateService(TeammateRepository teammateRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.teammateRepository = teammateRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public TeammatesSummaryResponse getAllTeammatesSummary() {
        logger.info("Fetching all teammates summary.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Teammate> teammatesToConsider;
        if ("ADMIN".equalsIgnoreCase(userDetails.getRole()) || "HR".equalsIgnoreCase(userDetails.getRole())) {
            teammatesToConsider = teammateRepository.findAll(Sort.by(Sort.Direction.ASC, "user.fullName"));
        } else if (userDetails.getProjectIds() != null && !userDetails.getProjectIds().isEmpty()) {
            // FIX: Calling the corrected repository method
            teammatesToConsider = teammateRepository.findByProjects_IdIn(userDetails.getProjectIds(), Sort.by(Sort.Direction.ASC, "user.fullName"));
        } else {
            return new TeammatesSummaryResponse(0, 0, 0, 0, Collections.emptyList());
        }

        long totalMembers = teammatesToConsider.size();
        long availableMembers = teammatesToConsider.stream().filter(t -> "Free".equals(t.getAvailabilityStatus())).count();
        long occupiedMembers = totalMembers - availableMembers;

        List<TeammateResponse> teammateResponses = teammatesToConsider.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new TeammatesSummaryResponse(totalMembers, availableMembers, occupiedMembers, 0, teammateResponses);
    }

    public TeammateResponse getTeammateById(Long id) {
        Teammate teammate = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));
        return convertToDto(teammate);
    }

    @Transactional
    public TeammateResponse updateTeammate(Long teammateId, TeammateCreateRequest request) {
        logger.info("Received request to update teammate ID: {}", teammateId);
        Teammate existingTeammate = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + teammateId));

        User user = existingTeammate.getUser();
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
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
        logger.info("Received request to delete teammate ID: {}", teammateId);
        Teammate teammateToDelete = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + teammateId));

        boolean isAssignedToActiveTask = teammateToDelete.getAssignedTasks().stream()
                .anyMatch(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED);
        if (isAssignedToActiveTask) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate is assigned to active tasks and cannot be deleted.");
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
                    logger.info("Teammate '{}' availability changed to: {}", teammate.getName(), newStatus);
                }
            });
        });
    }

    private boolean calculateIsOccupied(Teammate teammate) {
        return teammate.getAssignedTasks().stream()
                .anyMatch(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED);
    }

    private TeammateResponse convertToDto(Teammate teammate) {
        List<Long> projectIds = teammate.getProjects().stream().map(Project::getProjectId).collect(Collectors.toList());
        List<String> projectNames = teammate.getProjects().stream().map(Project::getProjectName).collect(Collectors.toList());
        long activeTasks = teammate.getAssignedTasks().stream().filter(task -> task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CLOSED).count();
        long completedTasks = teammate.getAssignedTasks().size() - activeTasks;

        return new TeammateResponse(
                teammate.getTeammateId(), teammate.getName(), teammate.getEmail(), teammate.getRole(),
                teammate.getPhone(), teammate.getDepartment(), teammate.getLocation(), teammate.getAvatar(),
                teammate.getAvailabilityStatus(), activeTasks, completedTasks, projectIds, projectNames
        );
    }
}
