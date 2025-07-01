package com.olive.controller;

import com.olive.dto.UserCreateUpdateRequest;
import com.olive.dto.UserResponse;
import com.olive.service.UserService;
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
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Only ADMIN can create/onboard users (and assign role/project)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateUpdateRequest request) {
        logger.info("Received request to create user by admin: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ADMIN and HR can get all users (global view)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Received request to get all users.");
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    // ADMIN and HR can get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("Received request to get user by ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    // ADMIN can update any user. HR can update non-ADMIN/HR users to assign projects.
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserCreateUpdateRequest request) {
        logger.info("Received request to update user ID: {}", id);
        // Authorization logic for HR is handled within UserService.updateUser
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    // Only ADMIN can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Received request to delete user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
