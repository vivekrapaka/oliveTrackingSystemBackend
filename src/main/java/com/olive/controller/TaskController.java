package com.olive.controller;

import com.olive.dto.*;
import com.olive.model.Task;
import com.olive.model.User;
import com.olive.repository.TaskRepository;
import com.olive.repository.UserRepository;
import com.olive.security.UserDetailsImpl;
import com.olive.service.TaskActivityService;
import com.olive.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    @Autowired
    private TaskActivityService taskActivityService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }



    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA', 'TEAMMEMBER')")
    public ResponseEntity<TasksSummaryResponse> getAllTasks(@RequestParam(required = false) String taskName) {
        return ResponseEntity.ok(taskService.getAllTasks(taskName));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateUpdateRequest request) {
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA', 'TEAMMEMBER')")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskCreateUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/generateSequenceNumber")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA')")
    public ResponseEntity<Long> getNextTaskSequenceNumber() {
        return ResponseEntity.ok(taskService.generateNextSequenceNumber());
    }

    // NEW: Endpoint to add a comment
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAMLEAD', 'BA', 'TEAMMEMBER')")
    public ResponseEntity<Void> addComment(@PathVariable Long taskId, @Valid @RequestBody CommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId()).orElse(null);

        taskActivityService.addComment(task, currentUser, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // NEW: Endpoint to get task history
    @GetMapping("/{taskId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Only management can see history
    public ResponseEntity<List<TaskActivityResponse>> getTaskHistory(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskActivityService.getTaskHistory(taskId));
    }
}
