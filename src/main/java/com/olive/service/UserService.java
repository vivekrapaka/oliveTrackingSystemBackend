package com.olive.service;

import com.olive.dto.UserCreateUpdateRequest;
import com.olive.dto.UserResponse;
import com.olive.model.Project;
import com.olive.model.Role;
import com.olive.model.Teammate;
import com.olive.model.User;
import com.olive.repository.ProjectRepository;
import com.olive.repository.RoleRepository;
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
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, TeammateRepository teammateRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.teammateRepository = teammateRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return convertToDto(user);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public UserResponse createUser(UserCreateUpdateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
        }
        Role role = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Role ID."));
        validateProjectAssignmentForRole(role, request.getProjectIds());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setProjectIds(request.getProjectIds() != null ? new ArrayList<>(request.getProjectIds()) : new ArrayList<>());
        User savedUser = userRepository.save(user);

        if (!"ADMIN".equalsIgnoreCase(role.getFunctionalGroup()) && !"HR".equalsIgnoreCase(role.getFunctionalGroup())) {
            createOrUpdateTeammateForUser(savedUser, request.getPhone(), request.getLocation());
        }
        return convertToDto(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserCreateUpdateRequest request) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Role newRole = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Role ID."));
        validateProjectAssignmentForRole(newRole, request.getProjectIds());

        existingUser.setFullName(request.getFullName());
        existingUser.setRole(newRole);
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getProjectIds() != null) {
            existingUser.setProjectIds(new ArrayList<>(request.getProjectIds()));
        }
        User updatedUser = userRepository.save(existingUser);

        if (!"ADMIN".equalsIgnoreCase(newRole.getFunctionalGroup()) && !"HR".equalsIgnoreCase(newRole.getFunctionalGroup())) {
            createOrUpdateTeammateForUser(updatedUser, request.getPhone(), request.getLocation());
        } else {
            teammateRepository.findByUser(updatedUser).ifPresent(teammateRepository::delete);
        }
        return convertToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        teammateRepository.findByUser(user).ifPresent(teammateRepository::delete);
        userRepository.delete(user);
    }

    private UserResponse convertToDto(User user) {
        List<String> projectNames = user.getProjectIds().stream()
                .map(projectId -> projectRepository.findById(projectId).map(Project::getProjectName).orElse("Unknown Project"))
                .collect(Collectors.toList());
        String phone = null;
        String location = null;
        Optional<Teammate> teammateOpt = teammateRepository.findByUser(user);
        if (teammateOpt.isPresent()) {
            Teammate teammate = teammateOpt.get();
            phone = teammate.getPhone();
            location = teammate.getLocation();
        }
        // FIX: Pass the functionalGroup to the UserResponse constructor
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getTitle(),
                user.getRole().getFunctionalGroup(),
                user.getProjectIds(),
                projectNames,
                phone,
                location
        );
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

    private void validateProjectAssignmentForRole(Role role, List<Long> projectIds) {
        if (projectIds == null) projectIds = Collections.emptyList();
        String functionalGroup = role.getFunctionalGroup();
        if ("ADMIN".equalsIgnoreCase(functionalGroup) || "HR".equalsIgnoreCase(functionalGroup)) {
            if (!projectIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADMIN/HR roles cannot be assigned to projects.");
            }
        } else {
            if (projectIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This role must be assigned to at least one project.");
            }
        }
    }
}