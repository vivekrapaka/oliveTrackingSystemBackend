package com.olive.service;

import com.olive.dto.*;
import com.olive.model.User;
import com.olive.repository.UserRepository;
import com.olive.security.JwtTokenUtil;
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
    public MessageResponse registerUser(SignupRequest signUpRequest) { // Changed return type to MessageResponse
        logger.info("Attempting to register user with email: {}", signUpRequest.getEmail());

        // Check if email already exists
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            logger.warn("Signup failed: Email {} is already in use.", signUpRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use!");
        }

        // Create new user's account
        User user = new User(signUpRequest.getFullName(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword())); // Hash password

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with email: {}", savedUser.getEmail());

        return new MessageResponse("User registered successfully!");
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) { // Changed parameter type to LoginRequest
        logger.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

        // Authenticate user using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Set the authenticated object in SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtTokenUtil.generateJwtToken(authentication);

        // Get UserDetails from the authenticated object
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        logger.info("User '{}' authenticated successfully. JWT generated.", loginRequest.getEmail());
        return new AuthResponse(jwt, userDetails.getId(), userDetails.getEmail(), userDetails.getFullName());
    }
}
