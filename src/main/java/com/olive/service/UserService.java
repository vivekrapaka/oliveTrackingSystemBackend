package com.olive.service;

import com.olive.dto.UserCreateUpdateRequest;
import com.olive.dto.UserResponse;
import com.olive.model.Project;
import com.olive.model.Teammate;
import com.olive.model.User;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final TeammateRepository teammateRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, TeammateRepository teammateRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.teammateRepository = teammateRepository;
    }

    // FIX: Updated convertToDto to include phone and location
    private UserResponse convertToDto(User user) {
        List<String> projectNames = user.getProjectIds().stream()
                .map(projectId -> projectRepository.findById(projectId).map(Project::getProjectName).orElse("Unknown Project"))
                .collect(Collectors.toList());

        // Fetch associated Teammate to get phone and location
        String phone = null;
        String location = null;
        if (!user.getRole().equalsIgnoreCase("ADMIN") && !user.getRole().equalsIgnoreCase("HR")) {
            Optional<Teammate> teammateOpt = teammateRepository.findByUser(user);
            if (teammateOpt.isPresent()) {
                Teammate teammate = teammateOpt.get();
                phone = teammate.getPhone();
                location = teammate.getLocation();
            }
        }

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getProjectIds(),
                projectNames,
                phone,      // Pass phone to constructor
                location    // Pass location to constructor
        );
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));
        return convertToDto(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateUpdateRequest request) {
        logger.info("Received request to create user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
        }
        validateProjectAssignmentForRole(request.getRole(), request.getProjectIds());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());
        user.setProjectIds(request.getProjectIds() != null ? new ArrayList<>(request.getProjectIds()) : new ArrayList<>());
        User savedUser = userRepository.save(user);

        if (!"ADMIN".equalsIgnoreCase(savedUser.getRole()) && !"HR".equalsIgnoreCase(savedUser.getRole())) {
            createOrUpdateTeammateForUser(savedUser, request.getPhone(), request.getLocation());
        }
        return convertToDto(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserCreateUpdateRequest request) {
        logger.info("Received request to update user with ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));

        validateProjectAssignmentForRole(request.getRole(), request.getProjectIds());

        existingUser.setFullName(request.getFullName());
        existingUser.setRole(request.getRole().toUpperCase());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getProjectIds() != null) {
            existingUser.setProjectIds(new ArrayList<>(request.getProjectIds()));
        }
        User updatedUser = userRepository.save(existingUser);

        if (!"ADMIN".equalsIgnoreCase(updatedUser.getRole()) && !"HR".equalsIgnoreCase(updatedUser.getRole())) {
            createOrUpdateTeammateForUser(updatedUser, request.getPhone(), request.getLocation());
        } else {
            teammateRepository.findByUser(updatedUser).ifPresent(teammateRepository::delete);
        }
        return convertToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        logger.info("Received request to delete user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));
        teammateRepository.findByUser(user).ifPresent(teammateRepository::delete);
        userRepository.delete(user);
    }

    @Transactional
    private void createOrUpdateTeammateForUser(User user, String phone, String location) {
        Teammate teammate = teammateRepository.findByUser(user).orElse(new Teammate());
        teammate.setUser(user);
        teammate.setPhone(phone);
        teammate.setLocation(location);

        String avatar = "";
        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            avatar = user.getFullName().substring(0, Math.min(user.getFullName().length(), 2)).toUpperCase();
        }
        teammate.setAvatar(avatar);

        Set<Project> assignedProjects = new HashSet<>(projectRepository.findAllById(user.getProjectIds()));
        teammate.setProjects(assignedProjects);
        teammateRepository.save(teammate);
    }

    private void validateProjectAssignmentForRole(String role, List<Long> projectIds) {
        if (projectIds == null) projectIds = Collections.emptyList();
        String upperCaseRole = role.toUpperCase();
        if (upperCaseRole.equals("ADMIN") || upperCaseRole.equals("HR")) {
            if (!projectIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, role + " role cannot be assigned to any projects.");
            }
        } else {
            if (projectIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, role + " role must be assigned to at least one project.");
            }
        }
    }
}
