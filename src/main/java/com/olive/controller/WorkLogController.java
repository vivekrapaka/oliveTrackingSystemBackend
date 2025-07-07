package com.olive.controller;

import com.olive.dto.WorkLogRequest;
import com.olive.dto.WorkLogResponse;
import com.olive.service.WorkLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WorkLogController {

    @Autowired
    private WorkLogService workLogService;

    @PostMapping("/tasks/{taskId}/worklogs")
    @PreAuthorize("hasAnyRole('TEAMMEMBER', 'DEVELOPER', 'TESTER')") // Roles that can log work
    public ResponseEntity<WorkLogResponse> logWork(@PathVariable Long taskId, @Valid @RequestBody WorkLogRequest request) {
        WorkLogResponse response = workLogService.logWork(taskId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{taskId}/worklogs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD')") // Roles that can view work logs
    public ResponseEntity<List<WorkLogResponse>> getWorkLogs(@PathVariable Long taskId) {
        return ResponseEntity.ok(workLogService.getWorkLogsForTask(taskId));
    }
}