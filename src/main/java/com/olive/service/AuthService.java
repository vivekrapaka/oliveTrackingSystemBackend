package com.olive.service;

import com.olive.dto.*;
import com.olive.model.Teammate;
import com.olive.model.User;
import com.olive.repository.ProjectRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.UserRepository;
import com.olive.security.JwtTokenUtil;
import com.olive.security.UserDetailsImpl;
import com.olive.security.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.olive.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // Added
import org.springframework.security.core.context.SecurityContextHolder; // Added
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeammateRepository teammateRepository; // NEW

    @Autowired
    private ProjectRepository projectRepository; // NEW

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Transactional
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        logger.info("Attempting to register user with email: {}", signUpRequest.getEmail());

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            logger.warn("Signup failed: Email {} is already in use.", signUpRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use!");
        }
        if (teammateRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            logger.warn("Signup failed: Teammate with email {} already exists.", signUpRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use by a teammate!");
        }

        // Create new user's account with default role "TEAMMEMBER" and no project initially
        User user = new User(signUpRequest.getFullName(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                "TEAMMEMBER", // Default role for self-signup
                Collections.emptyList()); // No projects assigned at signup

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with email: {}", savedUser.getEmail());

        // Also create a corresponding Teammate record
        Teammate teammate = new Teammate();
        teammate.setName(signUpRequest.getFullName());
        teammate.setEmail(signUpRequest.getEmail());
        teammate.setRole("TEAMMEMBER"); // Teammate role matches user role for self-signup
        teammate.setPhone(signUpRequest.getPhone());
        teammate.setLocation(signUpRequest.getLocation());

        // Generate avatar from full name's first two letters
        String avatar = "";
        if (signUpRequest.getFullName() != null && signUpRequest.getFullName().length() >= 2) {
            avatar = signUpRequest.getFullName().substring(0, 2).toUpperCase();
        } else if (signUpRequest.getFullName() != null && signUpRequest.getFullName().length() == 1) {
            avatar = signUpRequest.getFullName().toUpperCase();
        } else {
            avatar = "NA"; // Default for empty/short names
        }
        teammate.setAvatar(avatar);
        teammate.setAvailabilityStatus("Free"); // Default status

        // For self-signed-up TEAMMEMBER, project is null initially for Teammate too.
        teammate.setProjectId(null);

        teammateRepository.save(teammate);
        logger.info("Corresponding Teammate record created for user: {}", teammate.getEmail());

        return new MessageResponse("User registered successfully!");
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenUtil.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Fetch project names for the user's assigned project IDs
        List<String> projectNames = Collections.emptyList();
        if (userDetails.getProjectIds() != null && !userDetails.getProjectIds().isEmpty()) {
            projectNames = userDetails.getProjectIds().stream()
                    .map(projectId -> projectRepository.findById(projectId).map(Project::getProjectName).orElse("Unknown Project"))
                    .collect(Collectors.toList());
        }

        logger.info("User '{}' authenticated successfully. JWT generated. Role: {}, Project IDs: {}",
                loginRequest.getEmail(), userDetails.getRole(), userDetails.getProjectIds());

        return new AuthResponse(jwt, userDetails.getId(), userDetails.getEmail(),
                userDetails.getFullName(), userDetails.getRole(),
                userDetails.getProjectIds(), projectNames); // Return projectIds and projectNames
    }
}
