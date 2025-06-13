package com.olive.security;
/*
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService; // Inject UserDetailsServiceImpl

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API, though session-based usually wants it
                .authorizeHttpRequests(authorize -> authorize
                        // Allow registration and login without authentication
                        .requestMatchers("/auth/**").permitAll()
                        // Allow access to Swagger UI endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // All other API requests require authentication
                        .requestMatchers("/api/**").authenticated()
                        // Any other requests (e.g., static content) can be permitted or denied
                        .anyRequest().permitAll() // Adjust as needed for frontend static files
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login") // The URL to which the login form submits
                        .usernameParameter("email") // Specify email as the username parameter
                        .passwordParameter("password") // Specify password as the password parameter
                        .successHandler((request, response, authentication) -> {
                            // On successful login, send a 200 OK or appropriate response
                            // The frontend will receive a session cookie automatically
                            response.setStatus(200);
                            response.getWriter().write("{\"message\": \"Login successful\"}"); // Simple success message
                            response.getWriter().flush();
                        })
                        .failureHandler((request, response, exception) -> {
                            // On failed login, send a 401 Unauthorized or appropriate response
                            response.setStatus(401);
                            response.getWriter().write("{\"message\": \"Invalid credentials\"}");
                            response.getWriter().flush();
                        })
                        .permitAll() // Allow everyone to access the login form
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // URL to trigger logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.getWriter().write("{\"message\": \"Logout successful\"}");
                            response.getWriter().flush();
                        })
                        .permitAll() // Allow everyone to logout
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Spring Security defaults to this for session management
                )
                .userDetailsService(userDetailsService); // Set the custom UserDetailsService

        return http.build();
    }
}
*/