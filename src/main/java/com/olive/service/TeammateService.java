package com.olive.service;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
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
    private final TaskRepository taskRepository; // Inject TaskRepository to check assignments
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

    // Get all teammates
    public List<TeammateResponse> getAllTeammates() {
        return teammateRepository.findAll().stream()
                .map(this::convertToDto) // Now converts and updates status
                .collect(Collectors.toList());
    }

    // Get teammate by ID
    public TeammateResponse getTeammateById(Long id) {
        Teammate teammate = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));
        return convertToDto(teammate); // Now converts and updates status
    }

    // Create a new teammate
    public TeammateResponse createTeammate(TeammateCreateRequest request) {
        // Check for email uniqueness
        if (request.getEmail() != null && teammateRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
        }
        // Check for name uniqueness (since assignments rely on names)
        if (teammateRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this name already exists. Please use a unique name.");
        }

        Teammate teammate = new Teammate();
        teammate.setName(request.getName());
        teammate.setEmail(request.getEmail());
        teammate.setRole(request.getRole());
        teammate.setPhone(request.getPhone());
        teammate.setDepartment(request.getDepartment());
        teammate.setLocation(request.getLocation());
        teammate.setAvailabilityStatus("Free"); // Initially free, will be updated by TaskService logic
        return convertToDto(teammateRepository.save(teammate)); // convertToDto will calculate initial tasksAssigned/Completed
    }

    // Update teammate details
    public TeammateResponse updateTeammate(Long id, TeammateCreateRequest request) { // Reusing create request for update
        Teammate existingTeammate = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));

        // Check for name change and uniqueness if changed
        if (request.getName() != null && !request.getName().equalsIgnoreCase(existingTeammate.getName())) {
            if (teammateRepository.findByName(request.getName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this name already exists.");
            }
            // IMPORTANT: If name changes, you'd ideally update all tasks that reference this old name.
            // In this simplified model, tasks store names directly, making this complex.
            // For now, we update the teammate name. Re-calculating availability below ensures consistency.
            existingTeammate.setName(request.getName());
        }

        // Check for email uniqueness if updated
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existingTeammate.getEmail())) {
            Optional<Teammate> teammateWithSameEmail = teammateRepository.findByEmail(request.getEmail());
            if (teammateWithSameEmail.isPresent() && !teammateWithSameEmail.get().getTeammateId().equals(id)) {
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

    // Delete a teammate
    public void deleteTeammate(Long id) {
        Teammate teammateToDelete = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));

        // Before deleting, check if this teammate is assigned to any active (uncompleted) tasks.
        // If so, throw an error.
        boolean isAssignedToActiveTask = taskRepository.findAll().stream()
                .filter(task -> !task.getIsCompleted())
                .anyMatch(task -> {
                    if (task.getAssignedTeammateNames() == null || task.getAssignedTeammateNames().isEmpty()) {
                        return false;
                    }
                    return Arrays.asList(task.getAssignedTeammateNames().split(ASSIGNED_NAMES_DELIMITER)).contains(teammateToDelete.getName());
                });

        if (isAssignedToActiveTask) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teammate is currently assigned to active tasks and cannot be deleted. Please unassign them first.");
        }

        teammateRepository.deleteById(id);
    }
}
