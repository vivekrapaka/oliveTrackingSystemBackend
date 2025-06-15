package com.olive.service;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeammateService {

    private static final Logger logger = LoggerFactory.getLogger(TeammateService.class);

    private final TeammateRepository teammateRepository;
    private final TaskRepository taskRepository;
    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public TeammateService(TeammateRepository teammateRepository, TaskRepository taskRepository) {
        this.teammateRepository = teammateRepository;
        this.taskRepository = taskRepository;
    }

    // Helper to calculate isOccupied state based on assigned tasks
    private boolean calculateIsOccupied(Teammate teammate) {
        List<Task> allTasks = taskRepository.findAll();
        return allTasks.stream()
                .filter(task -> !task.getIsCompleted()) // Only consider non-completed tasks
                .anyMatch(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                            .map(String::trim)
                            .anyMatch(nameInTask -> nameInTask.equalsIgnoreCase(teammate.getName()));
                });
    }

    // Helper to convert Entity to DTO, also calculates availability dynamically
    private TeammateResponse convertToDto(Teammate teammate) {
        logger.debug("Converting Teammate entity to DTO: {}", teammate.getName());

        List<Task> allTasks = taskRepository.findAll();

        long activeTasksAssigned = allTasks.stream()
                .filter(task -> !task.getIsCompleted()) // Consider only non-completed tasks
                .filter(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    // Compare with stored uppercase name
                    return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                            .map(String::trim)
                            .anyMatch(name -> name.equalsIgnoreCase(teammate.getName()));
                })
                .count();

        long completedTasksAssigned = allTasks.stream()
                .filter(Task::getIsCompleted) // Consider only completed tasks
                .filter(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    // Compare with stored uppercase name
                    return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                            .map(String::trim)
                            .anyMatch(name -> name.equalsIgnoreCase(teammate.getName()));
                })
                .count();

        // Dynamically set availability status based on tasks
        String availabilityStatus = activeTasksAssigned > 0 ? "Occupied" : "Free";
        teammate.setAvailabilityStatus(availabilityStatus); // Update the entity's status for consistency

        TeammateResponse response = new TeammateResponse(
                teammate.getTeammateId(), // Maps to 'id' in frontend
                teammate.getName(),
                teammate.getEmail(),
                teammate.getRole(),
                teammate.getPhone(),
                teammate.getDepartment(),
                teammate.getLocation(),
                teammate.getAvatar(), // NEW: include avatar
                teammate.getAvailabilityStatus(), // Use the derived status
                activeTasksAssigned, // Include derived active tasks count
                completedTasksAssigned // Include derived completed tasks count
        );
        logger.debug("Converted Teammate DTO: {}", response);
        return response;
    }

    // Get all teammates with summary
    public TeammatesSummaryResponse getAllTeammatesSummary() {
        logger.info("Fetching all teammates summary.");
        List<Teammate> allTeammates = teammateRepository.findAll();
        List<Task> allTasks = taskRepository.findAll(); // Fetch all tasks for global counts

        long totalMembersInTeamCount = allTeammates.size();
        long availableTeamMembersCount = allTeammates.stream().filter(t -> "Free".equals(t.getAvailabilityStatus())).count();
        long occupiedTeamMembersCount = allTeammates.stream().filter(t -> "Occupied".equals(t.getAvailabilityStatus())).count();

        // Active tasks count (not in "Development" stage, global count)
        long activeTasksCount = allTasks.stream()
                .filter(task -> !task.getIsCompleted() && !task.getCurrentStage().equalsIgnoreCase("Development"))
                .count();

        List<TeammateResponse> teammateResponses = allTeammates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        TeammatesSummaryResponse summaryResponse = new TeammatesSummaryResponse(
                totalMembersInTeamCount,
                availableTeamMembersCount,
                occupiedTeamMembersCount,
                activeTasksCount,
                teammateResponses
        );
        logger.info("Returning TeammatesSummaryResponse with {} teammates.", summaryResponse.getTeammates().size());
        return summaryResponse;
    }


    // Get teammate by Name (changed from ID)
    public TeammateResponse getTeammateByName(String name) {
        logger.info("Attempting to retrieve teammate with name: {}", name);
        Teammate teammate = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    logger.warn("Teammate not found with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name);
                });
        logger.info("Successfully retrieved teammate: {}", teammate.getName());
        return convertToDto(teammate);
    }

    // Create a new teammate
    @Transactional
    public TeammateResponse createTeammate(TeammateCreateRequest request) {
        logger.info("Received request to create teammate with full name: {}", request.getFullName());
        String fullNameToSave = request.getFullName().trim();

        if (request.getEmail() != null && teammateRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Attempted to create teammate with duplicate email: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
        }
        if (fullNameToSave != null && teammateRepository.findByNameIgnoreCase(fullNameToSave).isPresent()) {
            logger.warn("Attempted to create teammate with duplicate name: {}", fullNameToSave);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this name (case-insensitive) already exists. Please use a unique name.");
        }

        Teammate teammate = new Teammate();
        teammate.setName(fullNameToSave);
        teammate.setEmail(request.getEmail());
        teammate.setRole(request.getRole());
        teammate.setPhone(request.getPhone());
        teammate.setDepartment(request.getDepartment());
        teammate.setLocation(request.getLocation());
        teammate.setAvatar(request.getAvatar());
        teammate.setAvailabilityStatus("Free"); // Default status initially, will be updated by task assignments

        logger.info("Saving new teammate: {}", teammate.getName());
        Teammate savedTeammate = teammateRepository.save(teammate);
        logger.info("Teammate saved successfully with ID: {}", savedTeammate.getTeammateId());
        return convertToDto(savedTeammate);
    }

    // Update teammate details by Name
    @Transactional
    public TeammateResponse updateTeammate(String name, TeammateCreateRequest request) {
        logger.info("Received request to update teammate: {}", name);
        Teammate existingTeammate = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    logger.warn("Teammate not found for update with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name);
                });
        logger.debug("Found existing teammate with ID: {}", existingTeammate.getTeammateId());

        String newFullNameToSave = request.getFullName().trim();

        // Handle name change and uniqueness
        if (newFullNameToSave != null && !newFullNameToSave.equalsIgnoreCase(existingTeammate.getName())) {
            Optional<Teammate> existingTeammateWithNewName = teammateRepository.findByNameIgnoreCase(newFullNameToSave);
            if (existingTeammateWithNewName.isPresent() && !existingTeammateWithNewName.get().getTeammateId().equals(existingTeammate.getTeammateId())) {
                logger.warn("Attempted to update teammate name to a duplicate: {}", newFullNameToSave);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with new name '" + newFullNameToSave + "' (case-insensitive) already exists.");
            }
            existingTeammate.setName(newFullNameToSave);
            logger.info("Teammate name updated to: {}", newFullNameToSave);
        }

        // Handle email change and uniqueness
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

        // After updating other details, ensure the availability status is correct based on tasks
        updateTeammateAvailability(updatedTeammate.getName());

        return convertToDto(updatedTeammate);
    }

    // Delete a teammate by Name
    @Transactional
    public void deleteTeammate(String name) {
        logger.info("Received request to delete teammate: {}", name);
        Teammate teammateToDelete = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    logger.warn("Teammate not found for deletion with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name);
                });
        logger.debug("Found teammate to delete with ID: {}", teammateToDelete.getTeammateId());

        boolean isAssignedToActiveTask = taskRepository.findAll().stream()
                .filter(task -> !task.getIsCompleted())
                .anyMatch(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    return Arrays.asList(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER)).contains(teammateToDelete.getName());
                });

        if (isAssignedToActiveTask) {
            logger.warn("Attempted to delete teammate '{}' who is assigned to active tasks.", name);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate is currently assigned to active tasks and cannot be deleted. Please unassign them first.");
        }

        teammateRepository.delete(teammateToDelete);
        logger.info("Teammate '{}' deleted successfully.", name);
    }

    // Helper to update a teammate's availability based on all their active tasks
    // This is crucial because availability is now derived from the 'tasks' table directly.
    @Transactional // Ensure this operation is transactional
    public void updateTeammateAvailability(String teammateName) {
        logger.debug("Attempting to update availability for teammate: {}", teammateName);
        teammateRepository.findByNameIgnoreCase(teammateName).ifPresent(teammate -> {
            boolean isOccupied = calculateIsOccupied(teammate); // Use helper for calculation
            String newStatus = isOccupied ? "Occupied" : "Free";
            if (!teammate.getAvailabilityStatus().equals(newStatus)) {
                teammate.setAvailabilityStatus(newStatus);
                teammateRepository.save(teammate); // Persist the change
                logger.info("Teammate '{}' availability changed to: {}", teammate.getName(), newStatus);
            } else {
                logger.debug("Teammate '{}' availability remains: {}", teammate.getName(), newStatus);
            }
        });
    }
}
