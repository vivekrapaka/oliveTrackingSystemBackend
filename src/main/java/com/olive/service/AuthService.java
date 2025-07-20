package com.olive.service;

import com.olive.dto.*;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private TeammateRepository teammateRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenUtil jwtTokenUtil;
    @Autowired private RoleRepository roleRepository;

    @Autowired private EmailService emailService;

    @Transactional
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Email is already in use!");
        }
        Role defaultRole = roleRepository.findByTitle("TEAM_MEMBER")
                .orElseThrow(() -> new RuntimeException("Error: Default Role 'TEAM_MEMBER' not found."));
        User user = new User(
                signUpRequest.getFullName(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                defaultRole,
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

        // FIX: Pass the functionalGroup to the AuthResponse constructor
        return new AuthResponse(jwt, userDetails.getId(), userDetails.getEmail(),
                userDetails.getFullName(), userDetails.getRoleTitle(), userDetails.getFunctionalGroup(),
                userDetails.getProjectIds(), projectNames);
    }

    @Transactional
    public void handleForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this email not found."));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1)); // Token is valid for 1 hour
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public MessageResponse handleResetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token."));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        return new MessageResponse("Password has been reset successfully.");
    }
}
