package com.olive.service;

import com.olive.dto.AuthResponse;
import com.olive.dto.LoginRequest;
import com.olive.dto.MessageResponse;
import com.olive.dto.SignupRequest;
import com.olive.model.Project;
import com.olive.model.Role;
import com.olive.model.Teammate;
import com.olive.model.User;
import com.olive.repository.ProjectRepository;
import com.olive.repository.RoleRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.UserRepository;
import com.olive.security.JwtTokenUtil;
import com.olive.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private TeammateRepository teammateRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenUtil jwtTokenUtil;
    @Autowired private RoleRepository roleRepository; // Added RoleRepository

    @Transactional
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Email is already in use!");
        }

        // Fetch the default role for self-signup
        Role defaultRole = roleRepository.findByTitle("TEAM_MEMBER")
                .orElseThrow(() -> new RuntimeException("Error: Default Role 'TEAM_MEMBER' not found."));

        User user = new User(
                signUpRequest.getFullName(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                defaultRole, // Assign the Role object
                Collections.emptyList()
        );
        User savedUser = userRepository.save(user);

        Teammate teammate = new Teammate();
        teammate.setUser(savedUser);
        teammate.setPhone(signUpRequest.getPhone());
        teammate.setLocation(signUpRequest.getLocation());

        String avatar = "";
        if (signUpRequest.getFullName() != null && !signUpRequest.getFullName().isEmpty()) {
            avatar = signUpRequest.getFullName().substring(0, Math.min(signUpRequest.getFullName().length(), 2)).toUpperCase();
        }
        teammate.setAvatar(avatar);

        teammateRepository.save(teammate);
        return new MessageResponse("User registered successfully!");
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenUtil.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> projectNames = userDetails.getProjectIds().stream()
                .map(projectId -> projectRepository.findById(projectId)
                        .map(Project::getProjectName)
                        .orElse("Unknown Project"))
                .collect(Collectors.toList());

        return new AuthResponse(jwt, userDetails.getId(), userDetails.getEmail(),
                userDetails.getFullName(), userDetails.getRoleTitle(), // Use getRoleTitle()
                userDetails.getProjectIds(), projectNames);
    }
}
