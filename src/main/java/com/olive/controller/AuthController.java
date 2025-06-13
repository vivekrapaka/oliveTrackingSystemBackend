package com.olive.controller;
/*
import com.olive.dto.AuthRequest;
import com.olive.dto.AuthResponse;
import com.olive.dto.RegisterRequest;
import com.olive.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Added for logout
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Allow frontend origins
public class AuthController {

    private final AuthService authService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler(); // For logout

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // The actual authentication is handled by Spring Security's formLogin filter.
        // This controller method will simply return the user details if login is successful.
        // If login fails, Spring Security's failure handler will respond.
        AuthResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        this.logoutHandler.logout(request, response, authentication); // Invalidate session
        return ResponseEntity.ok("Successfully logged out.");
    }
}
*/