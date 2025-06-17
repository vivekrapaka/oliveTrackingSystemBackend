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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'BA', 'TEAM_MEMBER')")
    public ResponseEntity<TeammatesSummaryResponse> getAllTeammatesSummary() {
        logger.info("Received request to get all teammates summary.");
        TeammatesSummaryResponse response = teammateService.getAllTeammatesSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'BA', 'TEAM_MEMBER')")
    public ResponseEntity<TeammateResponse> getTeammateByName(@PathVariable String name) {
        logger.info("Received request to get teammate by name: {}", name);
        TeammateResponse response = teammateService.getTeammateByName(name);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeammateResponse> createTeammate(@Valid @RequestBody TeammateCreateRequest request) {
        logger.info("Received request to create teammate: {}", request.getFullName());
        TeammateResponse response = teammateService.createTeammate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeammateResponse> updateTeammate(@PathVariable String name, @Valid @RequestBody TeammateCreateRequest request) {
        logger.info("Received request to update teammate '{}'.", name);
        TeammateResponse response = teammateService.updateTeammate(name, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeammate(@PathVariable String name) {
        logger.info("Received request to delete teammate '{}'.", name);
        teammateService.deleteTeammate(name);
        return ResponseEntity.noContent().build();
    }
}
