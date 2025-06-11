package com.olive.controller;

import com.olive.dto.TeammateAvailabilityUpdateRequest;
import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.service.TeammateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teammates")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Allow requests from frontend origin
public class TeammateController {

    private final TeammateService teammateService;

    @Autowired
    public TeammateController(TeammateService teammateService) {
        this.teammateService = teammateService;
    }

    // GET all teammates
    @GetMapping
    public ResponseEntity<List<TeammateResponse>> getAllTeammates() {
        List<TeammateResponse> teammates = teammateService.getAllTeammates();
        return ResponseEntity.ok(teammates);
    }

    // GET teammate by ID
    @GetMapping("/{id}")
    public ResponseEntity<TeammateResponse> getTeammateById(@PathVariable Long id) {
        TeammateResponse teammate = teammateService.getTeammateById(id);
        return ResponseEntity.ok(teammate);
    }

    // POST create a new teammate
    @PostMapping
    public ResponseEntity<TeammateResponse> createTeammate(@Valid @RequestBody TeammateCreateRequest request) {
        TeammateResponse newTeammate = teammateService.createTeammate(request);
        return new ResponseEntity<>(newTeammate, HttpStatus.CREATED);
    }

    // PUT update an existing teammate (full update or specific fields like name/email)
    @PutMapping("/{id}")
    public ResponseEntity<TeammateResponse> updateTeammate(@PathVariable Long id, @Valid @RequestBody TeammateCreateRequest request) {
        TeammateResponse updatedTeammate = teammateService.updateTeammate(id, request);
        return ResponseEntity.ok(updatedTeammate);
    }

    // PATCH update teammate availability status (more specific update)
    @PatchMapping("/{id}/availability")
    public ResponseEntity<TeammateResponse> updateTeammateAvailability(@PathVariable Long id, @Valid @RequestBody TeammateAvailabilityUpdateRequest request) {
        // Service method returns Teammate, convert to DTO for response
        TeammateResponse updatedTeammate = teammateService.updateTeammateAvailability(id, request.getAvailabilityStatus())
                .convertToDto(); // Assuming convertToDto is accessible or add a public static method in TeammateResponse
        return ResponseEntity.ok(updatedTeammate);
    }


    // DELETE a teammate
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeammate(@PathVariable Long id) {
        teammateService.deleteTeammate(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
