package com.olive.service;

import com.olive.dto.UserCreateUpdateRequest;
import com.olive.dto.UserResponse;
import com.olive.model.User;
import com.olive.repository.ProjectRepository;
import com.olive.repository.UserRepository;
import com.olive.model.Project;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository; // NEW: Inject ProjectRepository


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
    }

    private UserResponse convertToDto(User user) {
        String projectName = null;
        if (user.getProjectId() != null) {
            projectName = projectRepository.findById(user.getProjectId())
                    .map(Project::getProjectName)
                    .orElse("Unknown Project");
        }
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getProjectId(), projectName);
    }

    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users.");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        logger.info("Attempting to retrieve user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
                });
        return convertToDto(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateUpdateRequest request) {
        logger.info("Received request to create user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("User creation failed: Email {} is already in use.", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
        }

        if (request.getProjectId() != null) {
            projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found with ID: " + request.getProjectId()));
        }

        // Validate that ADMIN role doesn't have a projectId, and non-ADMIN roles must have one
        if ("ADMIN".equalsIgnoreCase(request.getRole()) && request.getProjectId() != null) {
            logger.warn("Admin user creation failed: ADMIN role cannot be assigned a projectId.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADMIN role cannot be assigned a project ID.");
        }
        if (!"ADMIN".equalsIgnoreCase(request.getRole()) && request.getProjectId() == null) {
            logger.warn("User creation failed: Non-ADMIN role requires a projectId.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Non-ADMIN roles (MANAGER, BA, TEAM_MEMBER) must be assigned a project ID.");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Password is mandatory for creation
        user.setRole(request.getRole().toUpperCase()); // Store role in uppercase
        user.setProjectId(request.getProjectId());

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return convertToDto(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserCreateUpdateRequest request) {
        logger.info("Received request to update user with ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found for update with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
                });

        // Handle email change and uniqueness
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(request.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(existingUser.getId())) {
                logger.warn("Attempted to update user email to a duplicate: {}", request.getEmail());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
            }
            existingUser.setEmail(request.getEmail());
            logger.debug("Updated email to: {}", request.getEmail());
        }

        Optional.ofNullable(request.getFullName()).ifPresent(existingUser::setFullName);
        Optional.ofNullable(request.getPassword())
                .filter(p -> !p.isEmpty()) // Only update if password is provided and not empty
                .ifPresent(p -> existingUser.setPassword(passwordEncoder.encode(p)));

        // Handle role and projectId updates
        if (request.getRole() != null && !request.getRole().equalsIgnoreCase(existingUser.getRole())) {
            String newRole = request.getRole().toUpperCase();
            // Validate ADMIN role doesn't have a projectId, and non-ADMIN roles must have one
            if ("ADMIN".equalsIgnoreCase(newRole) && request.getProjectId() != null) {
                logger.warn("User update failed: Cannot assign projectId to ADMIN role.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADMIN role cannot be assigned a project ID.");
            }
            if (!"ADMIN".equalsIgnoreCase(newRole) && request.getProjectId() == null) {
                logger.warn("User update failed: Non-ADMIN role requires a projectId.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Non-ADMIN roles (MANAGER, BA, TEAM_MEMBER) must be assigned a project ID.");
            }
            existingUser.setRole(newRole);
            logger.debug("Updated role to: {}", newRole);
        }

        // Handle projectId update
        // If the role implies a projectId is needed, and the projectId is provided, validate it.
        // If the role is changing to ADMIN, projectId should become null.
        if (!"ADMIN".equalsIgnoreCase(request.getRole())) { // If new role is not ADMIN, projectId should be set
            if (request.getProjectId() != null) {
                projectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found with ID: " + request.getProjectId()));
                existingUser.setProjectId(request.getProjectId());
            } else {
                // This means a non-ADMIN role is being set/updated without a projectId, which is an error
                logger.warn("User update failed: Non-ADMIN role requires a projectId but none was provided.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Non-ADMIN roles (MANAGER, BA, TEAM_MEMBER) must be assigned a project ID.");
            }
        } else { // If new role is ADMIN, projectId should be null
            existingUser.setProjectId(null);
        }
        logger.debug("Updated projectId to: {}", existingUser.getProjectId());

        User updatedUser = userRepository.save(existingUser);
        logger.info("User with ID: {} updated successfully.", updatedUser.getId());
        return convertToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        logger.info("Received request to delete user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("User not found for deletion with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        logger.info("User with ID {} deleted successfully.", id);
    }
}
