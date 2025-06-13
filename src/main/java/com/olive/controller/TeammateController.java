package com.olive.controller;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
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

    // GET all teammates with summary statistics
    @GetMapping
    public ResponseEntity<TeammatesSummaryResponse> getAllTeammatesSummary() {
        TeammatesSummaryResponse summaryResponse = teammateService.getAllTeammatesSummary();
        return ResponseEntity.ok(summaryResponse);
    }

    // GET teammate by Name (changed from ID)
    @GetMapping("/{name}")
    public ResponseEntity<TeammateResponse> getTeammateByName(@PathVariable String name) {
        TeammateResponse teammate = teammateService.getTeammateByName(name);
        return ResponseEntity.ok(teammate);
    }

    // POST create a new teammate
    @PostMapping
    public ResponseEntity<TeammateResponse> createTeammate(@Valid @RequestBody TeammateCreateRequest request) {
        TeammateResponse newTeammate = teammateService.createTeammate(request);
        return new ResponseEntity<>(newTeammate, HttpStatus.CREATED);
    }

    // PUT update an existing teammate by Name (changed from ID)
    @PutMapping("/{name}")
    public ResponseEntity<TeammateResponse> updateTeammate(@PathVariable String name, @Valid @RequestBody TeammateCreateRequest request) {
        TeammateResponse updatedTeammate = teammateService.updateTeammate(name, request);
        return ResponseEntity.ok(updatedTeammate);
    }


    // DELETE a teammate by Name (changed from ID)
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteTeammate(@PathVariable String name) {
        teammateService.deleteTeammate(name);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
