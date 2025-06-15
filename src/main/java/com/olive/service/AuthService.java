package com.olive.service;
/*
import com.olive.dto.AuthRequest;
import com.olive.dto.AuthResponse;
import com.olive.dto.RegisterRequest;
import com.olive.model.User;
import com.olive.repository.UserRepository;
import com.olive.security.UserDetailsServiceImpl;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    // private final JwtUtil jwtUtil; // Removed

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService) { // Removed JwtUtil
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        // this.jwtUtil = jwtUtil; // Removed
    }

    public AuthResponse registerUser(RegisterRequest request) {
        // Check if user with given email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
        }

        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        // Encode the password before saving
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(newUser);

        // For registration, we don't necessarily need to automatically log them in
        // or return anything beyond confirmation of success.
        // If auto-login is desired, the frontend can call /auth/login after successful registration.
        return new AuthResponse(savedUser.getEmail(), savedUser.getFullName()); // No token
    }

    public AuthResponse loginUser(AuthRequest request) {
        try {
            // Authenticate user with Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            // If authentication is successful, the security context is updated by Spring Security
            // No need to manually set SecurityContextHolder.getContext().setAuthentication(authentication); here,
            // as it's typically handled by Spring Security's filters.
            SecurityContextHolder.getContext().setAuthentication(authentication);


        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Retrieve the full name for the response based on the authenticated email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found after successful authentication"));


        return new AuthResponse(user.getEmail(), user.getFullName()); // No token
    }
}
*/