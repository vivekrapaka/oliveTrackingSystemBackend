package com.olive.controller;

import com.olive.dto.TaskCreateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TaskUpdateRequest;
import com.olive.dto.TasksSummaryResponse;
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
@RestController
@RequestMapping("/api/tasks")
//@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8085"}) // Allow requests from frontend origin
public class TaskController {


    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA', 'TEAMMEMBER')")
    public ResponseEntity<TasksSummaryResponse> getAllTasks(@RequestParam(required = false) String taskName) {
        logger.info("Received request to get all tasks. Filter: {}", taskName);
        TasksSummaryResponse response = taskService.getAllTasks(taskName);
        logger.info("Returning {} tasks.", response.getTasks().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA')") // Only ADMIN, MANAGER, TEAMLEAD, BA can create tasks
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        logger.info("Received request to create task: {}", request.getTaskName());
        TaskResponse response = taskService.createTask(request);
        logger.info("Task created successfully with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA', 'TEAMMEMBER')") // Granular control in service
    public ResponseEntity<TaskResponse> updateTask(@PathVariable String name, @Valid @RequestBody TaskUpdateRequest request) {
        logger.info("Received request to update task '{}'.", name);
        TaskResponse response = taskService.updateTask(name, request);
        logger.info("Task '{}' updated successfully.", name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA')") // Only ADMIN, MANAGER, TEAMLEAD, BA can delete tasks
    public ResponseEntity<Void> deleteTask(@PathVariable String name) {
        logger.info("Received request to delete task '{}'.", name);
        taskService.deleteTask(name);
        logger.info("Task '{}' deleted successfully.", name);
        return ResponseEntity.noContent().build();
    }
}
