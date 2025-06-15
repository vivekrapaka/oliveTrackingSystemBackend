package com.olive.controller;


import com.olive.dto.AuthResponse;
import com.olive.dto.LoginRequest;

import com.olive.dto.MessageResponse;
import com.olive.dto.SignupRequest;
import com.olive.model.User;
import com.olive.repository.UserRepository;
import com.olive.security.JwtTokenUtil;

import com.olive.service.AuthService;
import com.olive.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Added for logout
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Allow frontend origins
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Received sign-in request for email: {}", loginRequest.getEmail());
        // Delegate authentication logic to AuthService, which returns AuthResponse
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Received sign-up request for email: {}", signUpRequest.getEmail());
        MessageResponse response = authService.registerUser(signUpRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
