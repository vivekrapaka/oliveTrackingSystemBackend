package com.olive.controller;

import com.olive.dto.TaskActivityResponse;
import com.olive.dto.TaskCreateUpdateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TasksSummaryResponse;
import com.olive.model.Task;
import com.olive.repository.TaskRepository;
import com.olive.service.TaskActivityService;
import com.olive.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;
    private final TaskRepository taskRepository; // Inject repository for parent task lookup
    @Autowired
    TaskActivityService taskActivityService;
    @Autowired
    public TaskController(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER','TEST_MANAGER','DEV_LEAD','TEST_LEAD','BUSINESS_ANALYST', 'DEVELOPER', 'TESTER', 'TEAM_MEMBER',)")
    public ResponseEntity<TasksSummaryResponse> getAllTasks(@RequestParam(required = false) String taskName) {
        return ResponseEntity.ok(taskService.getAllTasks(taskName));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER','TEST_MANAGER','DEV_LEAD','TEST_LEAD','BUSINESS_ANALYST',)")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateUpdateRequest request) {
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_LEAD','TEST_LEAD', 'BUSINESS_ANALYST','TEST_MANAGER','DEV_MANAGER','DEVELOPER','TESTER')")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskCreateUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER','TEST_MANAGER',)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // FIX: Re-added a smarter endpoint for generating sequence numbers.
    @GetMapping("/nextSequenceNumber")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER','TEST_MANAGER','DEV_LEAD','TEST_LEAD','BUSINESS_ANALYST',)")
    public ResponseEntity<String> getNextTaskSequenceNumber(@RequestParam(required = false) Long parentId) {
        Task parentTask = null;
        if (parentId != null) {
            // Find the parent task to pass to the service method
            parentTask = taskRepository.findById(parentId).orElse(null);
        }
        String nextSequence = taskService.generateNextSequenceNumber(parentTask);
        return ResponseEntity.ok(nextSequence);
    }

    @GetMapping("/{taskId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER','DEV_MANAGER','TEST_MANAGER',)") // Only management can see history
    public ResponseEntity<List<TaskActivityResponse>> getTaskHistory(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskActivityService.getTaskHistory(taskId));
    }
}
