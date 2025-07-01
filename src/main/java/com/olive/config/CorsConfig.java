package com.olive.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply CORS to all paths
                        .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://localhost:8081", "http://localhost:8082","https://90fecb38-acb6-4af0-8018-b06ec94b9ce6.lovableproject.com","https://8080-ik70sazwr93axg03wafn6-93462d36.manusvm.computer","https://8080-igxhightz9kie4xcwsym1-8a736911.manusvm.computer/") // Allow specified origins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specified HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow sending of cookies/authentication headers
            }
        };
    }
}
