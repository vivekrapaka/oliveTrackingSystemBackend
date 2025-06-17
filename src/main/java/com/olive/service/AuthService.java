package com.olive.service;

import com.olive.dto.*;
import com.olive.model.User;
import com.olive.repository.UserRepository;
import com.olive.security.JwtTokenUtil;
import com.olive.security.UserDetailsImpl;
import com.olive.security.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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


@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

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

        // Create new user's account with default role and null projectId
        // Self-signed-up users are always TEAM_MEMBER and unassigned initially
        User user = new User(signUpRequest.getFullName(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                "TEAM_MEMBER", // Default role for self-signup
                null);         // Default projectId for self-signup (unassigned)

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with email: {}", savedUser.getEmail());

        return new MessageResponse("User registered successfully!");
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenUtil.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        logger.info("User '{}' authenticated successfully. JWT generated.", loginRequest.getEmail());
        // Return full user details including role and projectId
        return new AuthResponse(jwt, userDetails.getId(), userDetails.getEmail(), userDetails.getFullName(), userDetails.getRole(), userDetails.getProjectId());
    }
}
