package com.olive.service;

import com.olive.dto.UserCreateUpdateRequest;
import com.olive.dto.UserResponse;
import com.olive.model.Teammate;
import com.olive.model.User;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.UserRepository;
import com.olive.model.Project;
import com.olive.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final TeammateRepository teammateRepository; // NEW

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, TeammateRepository teammateRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.teammateRepository = teammateRepository;
    }

    private UserResponse convertToDto(User user) {
        List<String> projectNames = Collections.emptyList();
        if (user.getProjectIds() != null && !user.getProjectIds().isEmpty()) {
            projectNames = user.getProjectIds().stream()
                    .map(projectId -> projectRepository.findById(projectId).map(Project::getProjectName).orElse("Unknown Project"))
                    .collect(Collectors.toList());
        }
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getProjectIds(), projectNames);
    }

    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        List<User> users;
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            users = userRepository.findAll();
            logger.info("Admin user fetching all users globally.");
        } else if ("HR".equalsIgnoreCase(currentUser.getRole())) {
            // HR can see all users (including other HR, Managers, etc.) to manage their project assignments
            users = userRepository.findAll();
            logger.info("HR user fetching all users globally for management.");
        } else {
            logger.warn("User {} with role {} attempted to access all users. Access denied.", currentUser.getEmail(), currentUser.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You are not authorized to view all users.");
        }

        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        logger.info("Attempting to retrieve user with ID: {}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
                });

        // Authorization check: ADMIN can view any user. HR can view any user.
        // Other roles cannot view other users.
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole()) && !"HR".equalsIgnoreCase(currentUser.getRole())) {
            if (!currentUser.getId().equals(id)) { // Can view themselves
                logger.warn("User {} (Role {}) attempted to access user ID {} but is not authorized. Access denied.", currentUser.getEmail(), currentUser.getRole(), id);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You can only view your own user profile.");
            }
        }

        return convertToDto(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateUpdateRequest request) {
        logger.info("Received request to create user with email: {}", request.getEmail());
        // Only ADMIN can create users (enforced by controller @PreAuthorize)

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("User creation failed: Email {} is already in use.", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
        }

        // Validate project IDs if provided
        if (request.getProjectIds() != null && !request.getProjectIds().isEmpty()) {
            for (Long projectId : request.getProjectIds()) {
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found with ID: " + projectId));
            }
        }

        // Validate role-based project ID assignment
        validateProjectAssignmentForRole(request.getRole(), request.getProjectIds());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Password is mandatory for creation
        user.setRole(request.getRole().toUpperCase()); // Store role in uppercase
        user.setProjectIds(request.getProjectIds()); // Set list of project IDs

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        // Automatically create/link a Teammate record for non-ADMIN/HR roles
        if (!"ADMIN".equalsIgnoreCase(savedUser.getRole()) && !"HR".equalsIgnoreCase(savedUser.getRole())) {
            createOrUpdateTeammateForUser(savedUser);
            logger.info("Associated Teammate record created/updated for user: {}", savedUser.getEmail());
        }

        return convertToDto(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserCreateUpdateRequest request) {
        logger.info("Received request to update user with ID: {}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found for update with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
                });

        // Authorization: ADMIN can update any user. HR can update MANAGER/BA/TEAMLEAD/TEAMMEMBER users.
        if ("HR".equalsIgnoreCase(currentUser.getRole())) {
            if ("ADMIN".equalsIgnoreCase(existingUser.getRole()) || "HR".equalsIgnoreCase(existingUser.getRole())) {
                logger.warn("HR user {} attempted to update ADMIN/HR user {}. Access denied.", currentUser.getEmail(), existingUser.getEmail());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: HR cannot update ADMIN or other HR users.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            // Non-admin/HR users can only update themselves (if they even have update rights on this endpoint)
            if (!currentUser.getId().equals(id)) {
                logger.warn("User {} (Role {}) attempted to update user ID {} but is not authorized. Access denied.", currentUser.getEmail(), currentUser.getRole(), id);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You can only update your own user profile.");
            }
        }

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

        // Handle role update
        String newRole = request.getRole() != null ? request.getRole().toUpperCase() : existingUser.getRole();
        if ("HR".equalsIgnoreCase(currentUser.getRole()) && ("ADMIN".equalsIgnoreCase(newRole) || "HR".equalsIgnoreCase(newRole))) {
            // HR cannot change role to ADMIN or HR
            if (!newRole.equalsIgnoreCase(existingUser.getRole())) { // Only throw if trying to change to ADMIN/HR
                logger.warn("HR user {} attempted to change role of user {} to {}. Access denied.", currentUser.getEmail(), existingUser.getEmail(), newRole);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: HR cannot assign ADMIN or HR roles.");
            }
        }
        existingUser.setRole(newRole); // Update the role

        // Handle project IDs update
        List<Long> newProjectIds = request.getProjectIds();

        // Validate new project IDs exist
        if (newProjectIds != null && !newProjectIds.isEmpty()) {
            for (Long projectId : newProjectIds) {
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found with ID: " + projectId));
            }
        }

        // Validate role-based project ID assignment for the *new* role and provided project IDs
        validateProjectAssignmentForRole(newRole, newProjectIds);
        existingUser.setProjectIds(newProjectIds);

        User updatedUser = userRepository.save(existingUser);
        logger.info("User with ID: {} updated successfully.", updatedUser.getId());

        // Automatically create/link a Teammate record for non-ADMIN/HR roles, or update existing one
        if (!"ADMIN".equalsIgnoreCase(updatedUser.getRole()) && !"HR".equalsIgnoreCase(updatedUser.getRole())) {
            createOrUpdateTeammateForUser(updatedUser);
            logger.info("Associated Teammate record updated for user: {}", updatedUser.getEmail());
        } else {
            // If role changed to ADMIN or HR, ensure no linked teammate exists (or disassociate if logic requires)
            // For now, if a user becomes ADMIN/HR, their teammate record should ideally become defunct or be deleted.
            // Simplified: if a teammate exists for this email, and the user becomes ADMIN/HR, update its projectId to null.
            teammateRepository.findByEmail(updatedUser.getEmail()).ifPresent(teammate -> {
                if(teammate.getProjectId() != null) {
                    teammate.setProjectId(null); // Disassociate from any project
                    teammateRepository.save(teammate);
                    logger.info("Disassociated teammate '{}' (ID: {}) from project as user became ADMIN/HR.", teammate.getName(), teammate.getTeammateId());
                }
            });
        }

        return convertToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        logger.info("Received request to delete user with ID: {}", id);
        // Only ADMIN can delete users (enforced by controller @PreAuthorize)
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found for deletion with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
                });

        // Before deleting user, delete associated teammate if exists
        teammateRepository.findByEmail(userToDelete.getEmail()).ifPresent(teammateRepository::delete);
        logger.info("Associated Teammate record deleted for user: {}", userToDelete.getEmail());

        userRepository.deleteById(id);
        logger.info("User with ID {} deleted successfully.", id);
    }


    // Helper method to validate project assignment based on role
    private void validateProjectAssignmentForRole(String role, List<Long> projectIds) {
        if ("ADMIN".equalsIgnoreCase(role) || "HR".equalsIgnoreCase(role)) {
            if (projectIds != null && !projectIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, role + " role cannot be assigned any project IDs.");
            }
        } else if ("TEAMLEAD".equalsIgnoreCase(role) || "TEAMMEMBER".equalsIgnoreCase(role)) {
            if (projectIds == null || projectIds.size() != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, role + " role must be assigned to exactly one project ID.");
            }
        } else if ("MANAGER".equalsIgnoreCase(role) || "BA".equalsIgnoreCase(role)) {
            if (projectIds == null || projectIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, role + " role must be assigned to at least one project ID.");
            }
        }
    }

    // Helper method to create or update Teammate record for a User
    @Transactional
    private void createOrUpdateTeammateForUser(User user) {
        Teammate teammate = teammateRepository.findByEmail(user.getEmail()).orElseGet(Teammate::new);

        teammate.setName(user.getFullName());
        teammate.setEmail(user.getEmail());
        teammate.setRole(user.getRole()); // Teammate role mirrors user's role
        // For self-signup, phone and location are set. For admin-created, they are null here.
        // Assuming user.phone and user.location are not directly on User entity after update.
        // We might need to fetch them from SignupRequest for initial creation, or pass them in UserCreateUpdateRequest if applicable.
        // For now, these might be null if user was created by Admin without these fields.
        // The `SignupRequest` has `phone` and `location`. The `UserCreateUpdateRequest` does not.
        // Let's ensure these are properly carried over if intended.
        // For simplicity: Teammate's phone/location might be left null if Admin creates user.
        // Only during signup will they be present.

        // Generate avatar from full name
        String avatar = "";
        if (user.getFullName() != null && user.getFullName().length() >= 2) {
            avatar = user.getFullName().substring(0, 2).toUpperCase();
        } else if (user.getFullName() != null && user.getFullName().length() == 1) {
            avatar = user.getFullName().toUpperCase();
        } else {
            avatar = "NA"; // Default for empty/short names
        }
        teammate.setAvatar(avatar);
        teammate.setAvailabilityStatus("Free"); // Default

        // Set projectId for teammate (only if user has a single project, i.e., TEAMLEAD, TEAMMEMBER)
        if (user.getProjectIds() != null && user.getProjectIds().size() == 1) {
            teammate.setProjectId(user.getProjectIds().get(0));
        } else {
            teammate.setProjectId(null); // For multi-project users (MANAGER, BA), a teammate is not linked to *all* projects.
            // For now, this implies teammates themselves are single-project.
            // If a MANAGER/BA creates a task, that task is tied to ONE project, and the assigned
            // teammate must be from *that* project.
            // This implies that while a MANAGER/BA user can belong to multiple projects,
            // a `Teammate` entity still represents a person in a *single* project context.
            // This is a design decision we've implicitly made.
        }

        teammateRepository.save(teammate);
    }
}
