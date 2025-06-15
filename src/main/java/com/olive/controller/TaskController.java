package com.olive.controller;

import com.olive.dto.TaskCreateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TaskUpdateRequest;
import com.olive.dto.TasksSummaryResponse;
import com.olive.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/tasks")
//@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8085"}) // Allow requests from frontend origin
public class TaskController {


    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET all tasks (no filter parameter here)
    @GetMapping
    public ResponseEntity<TasksSummaryResponse> getAllTasks() {
        // Pass null to indicate no specific name filter for this endpoint
        TasksSummaryResponse summaryResponse = taskService.getAllTasks(null);
        return ResponseEntity.ok(summaryResponse);
    }

    // NEW API: GET tasks filtered by name
    @GetMapping("/filterByName/{name}")
    public ResponseEntity<TasksSummaryResponse> getTasksByName(@PathVariable String name) {
        // Pass the name filter to the service
        TasksSummaryResponse summaryResponse = taskService.getAllTasks(name);
        return ResponseEntity.ok(summaryResponse);
    }

    // NEW API: Generate a unique sequence number for a new task
    @GetMapping("/generateSequenceNumber")
    public ResponseEntity<Long> generateSequenceNumber() {
        Long nextSequenceNumber = taskService.generateNextSequenceNumber();
        return ResponseEntity.ok(nextSequenceNumber);
    }

    // POST create a new task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse newTask = taskService.createTask(request);
        return new ResponseEntity<>(newTask, HttpStatus.CREATED);
    }

    // PUT update an existing task by name
    @PutMapping("/{name}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable String name, @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse updatedTask = taskService.updateTask(name, request);
        return ResponseEntity.ok(updatedTask);
    }

    // DELETE a task by name
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteTask(@PathVariable String name) {
        taskService.deleteTask(name);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
