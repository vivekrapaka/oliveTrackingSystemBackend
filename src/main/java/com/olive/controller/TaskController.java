package com.olive.controller;

import com.olive.dto.TaskCreateRequest;
import com.olive.dto.TaskResponse;
import com.olive.dto.TaskUpdateRequest;
import com.olive.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Allow requests from frontend origin
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET all tasks
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // GET task by ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    // POST create a new task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse newTask = taskService.createTask(request);
        return new ResponseEntity<>(newTask, HttpStatus.CREATED);
    }

    // PUT update an existing task
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse updatedTask = taskService.updateTask(id, request);
        return ResponseEntity.ok(updatedTask);
    }

    // POST assign a teammate to a task
    @PostMapping("/{taskId}/assign/{teammateId}")
    public ResponseEntity<TaskResponse> assignTeammate(@PathVariable Long taskId, @PathVariable Long teammateId) {
        TaskResponse updatedTask = taskService.assignTeammateToTask(taskId, teammateId);
        return ResponseEntity.ok(updatedTask);
    }

    // DELETE unassign a teammate from a task
    @DeleteMapping("/{taskId}/unassign/{teammateId}")
    public ResponseEntity<TaskResponse> unassignTeammate(@PathVariable Long taskId, @PathVariable Long teammateId) {
        TaskResponse updatedTask = taskService.unassignTeammateFromTask(taskId, teammateId);
        return ResponseEntity.ok(updatedTask);
    }

    // DELETE a task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
