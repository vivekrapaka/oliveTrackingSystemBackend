package com.olive.service;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
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
    private final TeammateRepository teammateRepository;
    private final TaskRepository taskRepository;
    private static final String ASSIGNED_NAMES_DELIMITER = ",";

    @Autowired
    public TeammateService(TeammateRepository teammateRepository, TaskRepository taskRepository) {
        this.teammateRepository = teammateRepository;
        this.taskRepository = taskRepository;
    }

    // Helper to convert Entity to DTO, now also calculates availability dynamically
    private TeammateResponse convertToDto(Teammate teammate) {
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


        String availabilityStatus = (activeTasksAssigned > 0) ? "Occupied" : "Free";
        // Optionally update the entity's availabilityStatus in DB here, though not strictly required if always derived
        if (!teammate.getAvailabilityStatus().equals(availabilityStatus)) {
            teammate.setAvailabilityStatus(availabilityStatus);
            teammateRepository.save(teammate); // Persist derived status
        }


        return new TeammateResponse(
                teammate.getTeammateId(),
                teammate.getName(),
                teammate.getEmail(),
                teammate.getRole(),
                teammate.getPhone(),
                teammate.getDepartment(),
                teammate.getLocation(),
                availabilityStatus, // Use the dynamically calculated status
                activeTasksAssigned, // Include derived active tasks count
                completedTasksAssigned // Include derived completed tasks count
        );
    }

    // Get all teammates with summary
    public TeammatesSummaryResponse getAllTeammatesSummary() {
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

        return new TeammatesSummaryResponse(
                totalMembersInTeamCount,
                availableTeamMembersCount,
                occupiedTeamMembersCount,
                activeTasksCount,
                teammateResponses
        );
    }


    // Get teammate by Name (changed from ID)
    public TeammateResponse getTeammateByName(String name) {
        // Use findByNameIgnoreCase for lookup
        Teammate teammate = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name));
        return convertToDto(teammate); // Now converts and updates status
    }

    // Create a new teammate
    public TeammateResponse createTeammate(TeammateCreateRequest request) {
        // Convert name to uppercase for uniqueness check and storage consistency
        String nameToSave = request.getName() != null ? request.getName().toUpperCase() : null;

        // Check for email uniqueness
        if (request.getEmail() != null && teammateRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
        }
        // Check for name uniqueness (case-insensitive)
        if (nameToSave != null && teammateRepository.findByNameIgnoreCase(nameToSave).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this name (case-insensitive) already exists. Please use a unique name.");
        }

        Teammate teammate = new Teammate();
        teammate.setName(nameToSave); // Set the uppercase name
        teammate.setEmail(request.getEmail());
        teammate.setRole(request.getRole());
        teammate.setPhone(request.getPhone());
        teammate.setDepartment(request.getDepartment());
        teammate.setLocation(request.getLocation());
        teammate.setAvailabilityStatus("Free"); // Initially free, will be updated by TaskService logic
        return convertToDto(teammateRepository.save(teammate)); // convertToDto will calculate initial tasksAssigned/Completed
    }

    // Update teammate details by Name (formerly updateTeammate using ID)
    public TeammateResponse updateTeammate(String name, TeammateCreateRequest request) {
        // Find existing teammate using case-insensitive name
        Teammate existingTeammate = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name));

        // If the name is being changed, ensure the new name is unique (case-insensitive)
        if (request.getName() != null && !request.getName().equalsIgnoreCase(existingTeammate.getName())) {
            String newNameToSave = request.getName().toUpperCase(); // Convert new name to uppercase for check
            Optional<Teammate> existingTeammateWithNewName = teammateRepository.findByNameIgnoreCase(newNameToSave);

            if (existingTeammateWithNewName.isPresent() && !existingTeammateWithNewName.get().getTeammateId().equals(existingTeammate.getTeammateId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with new name '" + request.getName() + "' (case-insensitive) already exists.");
            }
            existingTeammate.setName(newNameToSave); // Set the new uppercase name
        }

        // Check for email uniqueness if updated
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existingTeammate.getEmail())) {
            Optional<Teammate> teammateWithSameEmail = teammateRepository.findByEmail(request.getEmail());
            if (teammateWithSameEmail.isPresent() && !teammateWithSameEmail.get().getTeammateId().equals(existingTeammate.getTeammateId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
            }
            existingTeammate.setEmail(request.getEmail());
        }

        Optional.ofNullable(request.getRole()).ifPresent(existingTeammate::setRole);
        Optional.ofNullable(request.getPhone()).ifPresent(existingTeammate::setPhone);
        Optional.ofNullable(request.getDepartment()).ifPresent(existingTeammate::setDepartment);
        Optional.ofNullable(request.getLocation()).ifPresent(existingTeammate::setLocation);

        // Save updated teammate details
        teammateRepository.save(existingTeammate);
        // Recalculate availability and task counts after update
        return convertToDto(existingTeammate);
    }

    // Delete a teammate by Name (formerly deleteTeammate using ID)
    public void deleteTeammate(String name) {
        // Find teammate using case-insensitive name
        Teammate teammateToDelete = teammateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with name: " + name));

        // Before deleting, check if this teammate is assigned to any active (uncompleted) tasks.
        // If so, throw an error.
        boolean isAssignedToActiveTask = taskRepository.findAll().stream()
                .filter(task -> !task.getIsCompleted())
                .anyMatch(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    // Compare with stored uppercase name
                    return Arrays.asList(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER)).contains(teammateToDelete.getName());
                });

        if (isAssignedToActiveTask) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate is currently assigned to active tasks and cannot be deleted. Please unassign them first.");
        }

        teammateRepository.delete(teammateToDelete);
    }

    // Helper to update a teammate's availability based on all their active tasks
    // This is crucial because availability is now derived from the 'tasks' table directly.
    private void updateTeammateAvailability(String teammateName) {
        teammateRepository.findByNameIgnoreCase(teammateName).ifPresent(teammate -> { // Use case-insensitive find
            // A teammate is 'Occupied' if they are assigned to any NON-COMPLETED task.
            boolean isOccupied = taskRepository.findAll().stream()
                    .filter(task -> !task.getIsCompleted()) // Only consider non-completed tasks
                    .anyMatch(task -> {
                        if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                            return false;
                        }
                        // Compare with stored uppercase name (which is what .getName() will return now)
                        return Arrays.stream(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER))
                                .map(String::trim)
                                .anyMatch(nameInTask -> nameInTask.equalsIgnoreCase(teammate.getName()));
                    });

            String newStatus = isOccupied ? "Occupied" : "Free";
            if (!teammate.getAvailabilityStatus().equals(newStatus)) {
                teammate.setAvailabilityStatus(newStatus);
                teammateRepository.save(teammate);
            }
        });
    }
}
