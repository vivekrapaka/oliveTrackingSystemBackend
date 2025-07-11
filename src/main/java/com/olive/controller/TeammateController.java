package com.olive.controller;

import com.olive.dto.TeammateCreateRequest;
import com.olive.dto.TeammateResponse;
import com.olive.dto.TeammatesSummaryResponse;
import com.olive.service.TeammateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teammates")
public class TeammateController {

    private final TeammateService teammateService;

    @Autowired
    public TeammateController(TeammateService teammateService) {
        this.teammateService = teammateService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEV_MANAGER','TEST_MANAGER','DEV_LEAD','TEST_LEAD', 'MANAGER','BUSINESS_ANALYST')")
    public ResponseEntity<TeammatesSummaryResponse> getAllTeammatesSummary() {
        return ResponseEntity.ok(teammateService.getAllTeammatesSummary());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEV_MANAGER','TEST_MANAGER','DEV_LEAD','TEST_LEAD', 'MANAGER','BUSINESS_ANALYST')")
    public ResponseEntity<TeammateResponse> getTeammateById(@PathVariable Long id) {
        return ResponseEntity.ok(teammateService.getTeammateById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeammateResponse> updateTeammate(@PathVariable Long id, @Valid @RequestBody TeammateCreateRequest request) {
        return ResponseEntity.ok(teammateService.updateTeammate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeammate(@PathVariable Long id) {
        teammateService.deleteTeammate(id);
        return ResponseEntity.noContent().build();
    }
}
