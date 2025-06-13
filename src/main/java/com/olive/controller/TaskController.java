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
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8085"}) // Allow requests from frontend origin
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET all tasks or filter by name
    // This endpoint handles both /api/tasks (no param) and /api/tasks?name=keyword
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@RequestParam(required = false) String name) {
        List<TaskResponse> tasks;
        if (name != null && !name.trim().isEmpty()) {
            tasks = taskService.searchTasks(name);
        } else {
            tasks = taskService.getAllTasks();
        }
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

    // Removed explicit assignment endpoints as they are now handled within createTask/updateTask
    // @PostMapping("/{taskId}/assign/{teammateId}")
    // @DeleteMapping("/{taskId}/unassign/{teammateId}")

    // DELETE a task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
