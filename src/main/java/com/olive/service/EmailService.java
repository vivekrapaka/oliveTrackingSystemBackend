package com.olive.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendPasswordResetEmail(String userEmail, String token) {
        // In a real application, this would use JavaMailSender to send an email.
        // For now, we will log the reset link to the console for easy testing.
        String resetUrl = "http://your-frontend-url/reset-password?token=" + token;

        logger.info("--- PASSWORD RESET ---");
        logger.info("Sending password reset link to: {}", userEmail);
        logger.info("Reset Link: {}", resetUrl);
        logger.info("----------------------");
    }
}

