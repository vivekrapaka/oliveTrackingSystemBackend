package com.olive.service;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.model.TaskStage;
import com.olive.model.Teammate;
import com.olive.repository.TaskStageRepository;
import com.olive.repository.TeammateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeammateService {
    private final TeammateRepository teammateRepository;

    @Autowired
    public TeammateService(TeammateRepository teammateRepository) {
        this.teammateRepository = teammateRepository;
    }

    // Helper to convert Entity to DTO
    private TeammateResponse convertToDto(Teammate teammate) {
        return new TeammateResponse(
                teammate.getTeammateId(),
                teammate.getName(),
                teammate.getEmail(),
                teammate.getAvailabilityStatus()
        );
    }

    // Get all teammates
    public List<TeammateResponse> getAllTeammates() {
        return teammateRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get teammate by ID
    public TeammateResponse getTeammateById(Long id) {
        Teammate teammate = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));
        return convertToDto(teammate);
    }

    // Create a new teammate
    public TeammateResponse createTeammate(TeammateCreateRequest request) {
        // Basic validation for email uniqueness can be done here or relied on DB unique constraint
        if (request.getEmail() != null && teammateRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
        }

        Teammate teammate = new Teammate();
        teammate.setName(request.getName());
        teammate.setEmail(request.getEmail());
        // AvailabilityStatus defaults to "Free" in entity, no need to set here unless explicitly requested
        return convertToDto(teammateRepository.save(teammate));
    }

    // Update teammate details
    public TeammateResponse updateTeammate(Long id, TeammateCreateRequest request) { // Reusing create request for update, but could be separate DTO
        Teammate existingTeammate = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            existingTeammate.setName(request.getName());
        }
        if (request.getEmail() != null) {
            // Check for email uniqueness if updated
            Optional<Teammate> teammateWithSameEmail = teammateRepository.findByEmail(request.getEmail());
            if (teammateWithSameEmail.isPresent() && !teammateWithSameEmail.get().getTeammateId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Teammate with this email already exists.");
            }
            existingTeammate.setEmail(request.getEmail());
        }
        // Availability status should ideally be updated by task assignment/completion logic,
        // but if manual override is needed, a separate endpoint/DTO could handle it.

        return convertToDto(teammateRepository.save(existingTeammate));
    }

    // Update only availability status (used by task service)
    public Teammate updateTeammateAvailability(Long id, String status) {
        Teammate existingTeammate = teammateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id));
        existingTeammate.setAvailabilityStatus(status);
        return teammateRepository.save(existingTeammate);
    }

    // Delete a teammate
    public void deleteTeammate(Long id) {
        if (!teammateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + id);
        }
        // Consider logic to handle tasks assigned to this teammate before deleting (e.g., reassign, mark tasks unassigned)
        teammateRepository.deleteById(id);
    }

    // Initialize TaskStages (call this once, e.g., in an @EventListener on startup or manually)
    // This method would typically be in a separate data initializer service
    public void initializeTaskStages(TaskStageRepository taskStageRepository) {
        List<String> stages = List.of("SIT", "DEV", "Pre-Prod", "Prod");
        for (String stageName : stages) {
            if (taskStageRepository.findByStageName(stageName).isEmpty()) {
                taskStageRepository.save(new TaskStage(stageName));
            }
        }
    }
}
