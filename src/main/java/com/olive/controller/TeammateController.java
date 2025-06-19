package com.olive.controller;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
import com.olive.service.TeammateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/teammates")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Allow requests from frontend origin
public class TeammateController {

    private static final Logger logger = LoggerFactory.getLogger(TeammateController.class);

    private final TeammateService teammateService;

    @Autowired
    public TeammateController(TeammateService teammateService) {
        this.teammateService = teammateService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'TEAMLEAD', 'BA')") // TEAMMEMBER cannot see this tab
    public ResponseEntity<TeammatesSummaryResponse> getAllTeammatesSummary() {
        logger.info("Received request to get all teammates summary.");
        TeammatesSummaryResponse response = teammateService.getAllTeammatesSummary();
        logger.info("Returning teammates summary with {} total members.", response.getTotalMembersInTeamCount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'TEAMLEAD', 'BA')") // TEAMMEMBER cannot see this tab
    public ResponseEntity<TeammateResponse> getTeammateByName(@PathVariable String name) {
        logger.info("Received request to get teammate by name: {}", name);
        TeammateResponse response = teammateService.getTeammateByName(name);
        logger.info("Returning teammate details for: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can create teammates
    public ResponseEntity<TeammateResponse> createTeammate(@Valid @RequestBody TeammateCreateRequest request) {
        logger.info("Received request to create teammate: {}", request.getFullName());
        TeammateResponse response = teammateService.createTeammate(request);
        logger.info("Teammate created successfully with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can update teammates
    public ResponseEntity<TeammateResponse> updateTeammate(@PathVariable String name, @Valid @RequestBody TeammateCreateRequest request) {
        logger.info("Received request to update teammate '{}'.", name);
        TeammateResponse response = teammateService.updateTeammate(name, request);
        logger.info("Teammate '{}' updated successfully.", name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can delete teammates
    public ResponseEntity<Void> deleteTeammate(@PathVariable String name) {
        logger.info("Received request to delete teammate '{}'.", name);
        teammateService.deleteTeammate(name);
        logger.info("Teammate '{}' deleted successfully.", name);
        return ResponseEntity.noContent().build();
    }
}
