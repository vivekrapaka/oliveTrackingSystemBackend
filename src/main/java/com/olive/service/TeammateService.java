package com.olive.service;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.olive.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeammateService {

    private static final Logger logger = LoggerFactory.getLogger(TeammateService.class);

    private final TeammateRepository teammateRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository; // NEW: Inject ProjectRepository
    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public TeammateService(TeammateRepository teammateRepository, TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.teammateRepository = teammateRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository; // Initialize ProjectRepository
    }

    // Helper to calculate isOccupied state based on assigned tasks (now project-aware)
    private boolean calculateIsOccupied(Teammate teammate) {
        // Filter tasks by the teammate's project ID for relevance
        List<Task> relevantTasks = taskRepository.findByProjectId(teammate.getProjectId());
        return relevantTasks.stream()
                .filter(task -> !task.getIsCompleted())
                .anyMatch(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                            .map(String::trim)
                            .anyMatch(nameInTask -> nameInTask.equalsIgnoreCase(teammate.getName()));
                });
    }

    // Helper to convert Entity to DTO, also calculates availability dynamically and fetches project name
    private TeammateResponse convertToDto(Teammate teammate) {
        logger.debug("Converting Teammate entity to DTO: {}", teammate.getName());

        List<Task> allTasks = taskRepository.findAll(); // Used for global task counts if needed, but for assigned tasks, filter by project.

        // Get project name
        String projectName = projectRepository.findById(teammate.getProjectId())
                .map(Project::getProjectName)
                .orElse("Unknown Project"); // Default if project not found

        long activeTasksAssigned = allTasks.stream()
                .filter(task -> Objects.equals(task.getProjectId(), teammate.getProjectId())) // Filter by teammate's project
                .filter(task -> !task.getIsCompleted())
                .filter(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                            .map(String::trim)
                            .anyMatch(name -> name.equalsIgnoreCase(teammate.getName()));
                })
                .count();

        long completedTasksAssigned = allTasks.stream()
                .filter(task -> Objects.equals(task.getProjectId(), teammate.getProjectId())) // Filter by teammate's project
                .filter(Task::getIsCompleted)
                .filter(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                            .map(String::trim)
                            .anyMatch(name -> name.equalsIgnoreCase(teammate.getName()));
                })
                .count();

        String availabilityStatus = activeTasksAssigned > 0 ? "Occupied" : "Free";
        teammate.setAvailabilityStatus(availabilityStatus); // Update the entity's status for consistency

        TeammateResponse response = new TeammateResponse(
                teammate.getTeammateId(),
                teammate.getName(),
                teammate.getEmail(),
                teammate.getRole(),
                teammate.getPhone(),
                teammate.getDepartment(),
                teammate.getLocation(),
                teammate.getAvatar(),
                teammate.getAvailabilityStatus(),
                activeTasksAssigned,
                completedTasksAssigned,
                teammate.getProjectId(), // NEW: Include projectId
                projectName // NEW: Include projectName
        );
        logger.debug("Converted Teammate DTO: {}", response);
        return response;
    }

    // Get all teammates with summary - UPDATED (Project-aware filtering)
    public TeammatesSummaryResponse getAllTeammatesSummary() {
        logger.info("Fetching all teammates summary.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Teammate> teammatesToReturn;

        if ("ADMIN".equalsIgnoreCase(userDetails.getRole())) {
            logger.info("User is ADMIN, fetching all teammates globally.");
            teammatesToReturn = teammateRepository.findAll();
        } else if (userDetails.getProjectId() != null) {
            logger.info("User is {} from project ID {}. Fetching teammates for project ID: {}", userDetails.getRole(), userDetails.getProjectId(), userDetails.getProjectId());
            teammatesToReturn = teammateRepository.findByProjectId(userDetails.getProjectId());
        } else {
            logger.warn("User {} has role {} but no projectId assigned. Returning empty list for teammates.", userDetails.getEmail(), userDetails.getRole());
            teammatesToReturn = Collections.emptyList();
        }

        // Global task count for summary (still from all tasks for overall view on dashboard)
        List<Task> allTasks = taskRepository.findAll();
        long activeTasksCount = allTasks.stream()
                .filter(task -> !task.getIsCompleted() && !task.getCurrentStage().equalsIgnoreCase("Prod"))
                .count();

        long totalMembersInTeamCount = teammatesToReturn.size();
        long availableTeamMembersCount = teammatesToReturn.stream().filter(t -> "Free".equals(t.getAvailabilityStatus())).count();
        long occupiedTeamMembersCount = teammatesToReturn.stream().filter(t -> "Occupied".equals(t.getAvailabilityStatus())).count();

        List<TeammateResponse> teammateResponses = teammatesToReturn.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        TeammatesSummaryResponse summaryResponse = new TeammatesSummaryResponse(
                totalMembersInTeamCount,
                availableTeamMembersCount,
                occupiedTeamMembersCount,
                activeTasksCount,
                teammateResponses
        );
        logger.info("Returning TeammatesSummaryResponse with {} teammates visible to user.", summaryResponse.getTeammates().size());
        return summaryResponse;
    }

    // Get teammate by Name - UPDATED (Project-aware filtering)
    public TeammateResponse getTeammateByName(String name) {
        logger.info("Attempting to retrieve teammate with name: {}", name);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Teammate teammate;
        if ("ADMIN".equalsIgnoreCase(userDetails.getRole())) {
            teammate = teammateRepository.findByNameIgnoreCase(name) // Admin can find any teammate
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name));
        } else if (userDetails.getProjectId() != null) {
            teammate = teammateRepository.findByNameIgnoreCaseAndProjectId(name, userDetails.getProjectId()) // Non-admin can only find within their project
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name + " in your project."));
        } else {
            logger.warn("User {} with role {} has no projectId assigned. Access denied for teammate retrieval.", userDetails.getEmail(), userDetails.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You must be assigned to a project to view teammates.");
        }

        logger.info("Successfully retrieved teammate: {}", teammate.getName());
        return convertToDto(teammate);
    }

    // Create a new teammate - UPDATED (Admin-only, project-aware)
    @Transactional
    public TeammateResponse createTeammate(TeammateCreateRequest request) {
        logger.info("Received request to create teammate with full name: {}", request.getFullName());
        // This method is now expected to be called by ADMIN only,
        // and request contains the projectId for the new teammate.

        // Validate that the projectId exists
        projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found with ID: " + request.getProjectId()));

        String fullNameToSave = request.getFullName().trim();

        // Check for email uniqueness globally (User & Teammate emails should be unique across the system)
        if (request.getEmail() != null && teammateRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Attempted to create teammate with duplicate email: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
        }
        // Check for name uniqueness within the specified project
        if (fullNameToSave != null && teammateRepository.findByNameIgnoreCaseAndProjectId(fullNameToSave, request.getProjectId()).isPresent()) {
            logger.warn("Attempted to create teammate with duplicate name '{}' in project ID {}.", fullNameToSave, request.getProjectId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this name (case-insensitive) already exists in this project. Please use a unique name.");
        }

        Teammate teammate = new Teammate();
        teammate.setName(fullNameToSave);
        teammate.setEmail(request.getEmail());
        teammate.setRole(request.getRole());
        teammate.setPhone(request.getPhone());
        teammate.setDepartment(request.getDepartment());
        teammate.setLocation(request.getLocation());
        teammate.setAvatar(request.getAvatar());
        teammate.setAvailabilityStatus("Free");
        teammate.setProjectId(request.getProjectId()); // Assign projectId from request

        logger.info("Saving new teammate: {} for Project ID: {}", teammate.getName(), teammate.getProjectId());
        Teammate savedTeammate = teammateRepository.save(teammate);
        logger.info("Teammate saved successfully with ID: {}", savedTeammate.getTeammateId());
        return convertToDto(savedTeammate);
    }

    // Update teammate details by Name - UPDATED (Admin-only, project-aware)
    @Transactional
    public TeammateResponse updateTeammate(String name, TeammateCreateRequest request) {
        logger.info("Received request to update teammate: {}", name);
        // This method is now expected to be called by ADMIN only
        // The request may or may not contain a projectId. For existing teammates,
        // their projectId is retrieved from the DB, and only an Admin can change it (if a dedicated API for that is added).
        // For this general update, we validate the existing teammate's project against the request (if provided)
        // or ensure the Admin context is global.

        Teammate existingTeammate = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    logger.warn("Teammate not found for update with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name);
                });
        logger.debug("Found existing teammate with ID: {}", existingTeammate.getTeammateId());

        // For non-ADMIN users, we would restrict updates to their own project.
        // Since this method is now @PreAuthorize("hasRole('ADMIN')"), this check is simplified.
        // ADMIN can update any teammate. If the request includes projectId, validate it.
        if (request.getProjectId() != null && !request.getProjectId().equals(existingTeammate.getProjectId())) {
            // ADMIN is trying to change a teammate's project via a general update.
            // This is typically handled by a specific "reassign teammate to project" API for clarity.
            // For now, we'll allow ADMINs to directly change it here, but it's less explicit.
            projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target Project not found with ID: " + request.getProjectId()));
            existingTeammate.setProjectId(request.getProjectId());
            logger.info("Admin updated teammate '{}' projectId to {}", name, request.getProjectId());
        }

        String newFullNameToSave = request.getFullName().trim();

        // Handle name change and uniqueness within the *new or existing* project
        if (newFullNameToSave != null && !newFullNameToSave.equalsIgnoreCase(existingTeammate.getName())) {
            Optional<Teammate> existingTeammateWithNewName = teammateRepository.findByNameIgnoreCaseAndProjectId(newFullNameToSave, existingTeammate.getProjectId());
            if (existingTeammateWithNewName.isPresent() && !existingTeammateWithNewName.get().getTeammateId().equals(existingTeammate.getTeammateId())) {
                logger.warn("Attempted to update teammate name to a duplicate '{}' in project ID {}.", newFullNameToSave, existingTeammate.getProjectId());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with new name '" + newFullNameToSave + "' (case-insensitive) already exists in this project.");
            }
            existingTeammate.setName(newFullNameToSave);
            logger.info("Teammate name updated to: {}", newFullNameToSave);
        }

        // Handle email change and uniqueness globally (since emails are unique for Users too)
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existingTeammate.getEmail())) {
            Optional<Teammate> teammateWithSameEmail = teammateRepository.findByEmail(request.getEmail());
            if (teammateWithSameEmail.isPresent() && !teammateWithSameEmail.get().getTeammateId().equals(existingTeammate.getTeammateId())) {
                logger.warn("Attempted to update teammate email to a duplicate: {}", request.getEmail());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
            }
            existingTeammate.setEmail(request.getEmail());
            logger.debug("Updated email to: {}", request.getEmail());
        }

        // Update other fields
        Optional.ofNullable(request.getRole()).ifPresent(val -> { existingTeammate.setRole(val); logger.debug("Updated role."); });
        Optional.ofNullable(request.getPhone()).ifPresent(val -> { existingTeammate.setPhone(val); logger.debug("Updated phone."); });
        Optional.ofNullable(request.getDepartment()).ifPresent(val -> { existingTeammate.setDepartment(val); logger.debug("Updated department."); });
        Optional.ofNullable(request.getLocation()).ifPresent(val -> { existingTeammate.setLocation(val); logger.debug("Updated location."); });
        Optional.ofNullable(request.getAvatar()).ifPresent(val -> { existingTeammate.setAvatar(val); logger.debug("Updated avatar."); });

        logger.info("Saving updated teammate details for ID: {}", existingTeammate.getTeammateId());
        Teammate updatedTeammate = teammateRepository.save(existingTeammate);
        logger.info("Teammate updated successfully.");

        updateTeammateAvailability(updatedTeammate.getName());

        return convertToDto(updatedTeammate);
    }

    // Delete a teammate by Name - UPDATED (Admin-only, project-aware)
    @Transactional
    public void deleteTeammate(String name) {
        logger.info("Received request to delete teammate: {}", name);
        // This method is now expected to be called by ADMIN only.

        Teammate teammateToDelete = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    logger.warn("Teammate not found for deletion with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name);
                });
        logger.debug("Found teammate to delete with ID: {}", teammateToDelete.getTeammateId());

        // Check if assigned to any active tasks *within their project*
        boolean isAssignedToActiveTask = taskRepository.findByProjectIdAndAssignedTeammateNamesContaining(teammateToDelete.getProjectId(), teammateToDelete.getName()).stream()
                .anyMatch(task -> !task.getIsCompleted());

        if (isAssignedToActiveTask) {
            logger.warn("Attempted to delete teammate '{}' who is assigned to active tasks in Project ID {}.", name, teammateToDelete.getProjectId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate is currently assigned to active tasks and cannot be deleted. Please unassign them first.");
        }

        teammateRepository.delete(teammateToDelete);
        logger.info("Teammate '{}' deleted successfully.", name);
    }

    // Helper to update a teammate's availability based on all their active tasks
    // This is crucial because availability is now derived from the 'tasks' table directly.
    @Transactional
    public void updateTeammateAvailability(String teammateName) {
        logger.debug("Attempting to update availability for teammate: {}", teammateName);
        // Find teammate globally, as this helper might be called from task service and doesn't know project scope
        teammateRepository.findByNameIgnoreCase(teammateName).ifPresent(teammate -> {
            boolean isOccupied = calculateIsOccupied(teammate);
            String newStatus = isOccupied ? "Occupied" : "Free";
            if (!teammate.getAvailabilityStatus().equals(newStatus)) {
                teammate.setAvailabilityStatus(newStatus);
                teammateRepository.save(teammate);
                logger.info("Teammate '{}' availability changed to: {}", teammate.getName(), newStatus);
            } else {
                logger.debug("Teammate '{}' availability remains: {}", teammate.getName(), newStatus);
            }
        });
    }
}
