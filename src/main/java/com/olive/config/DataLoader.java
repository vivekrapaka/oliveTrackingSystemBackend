package com.olive.config;

import com.olive.model.Role;
import com.olive.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            createRoles();
        }
    }

    private void createRoles() {
        List<Role> roles = Arrays.asList(
                // Admin & HR
                new Role("ADMIN", "ADMIN"),
                new Role("HR", "HR"),

                // Management Roles (Segregated)
                new Role("SDM1", "DEV_MANAGER"),
                new Role("ASM1", "TEST_MANAGER"),
                new Role("BAM 1", "MANAGER"), // General Manager

                // Dev Lead Roles
                new Role("SDEIV", "DEV_LEAD"),
                new Role("SDEIII", "DEV_LEAD"),

                // Test Lead Role
                new Role("ASE 3", "TEST_LEAD"),

                // Developer Roles
                new Role("SDEII", "DEVELOPER"),
                new Role("SDE1", "DEVELOPER"),

                // Tester Roles
                new Role("ASE 1", "TESTER"),
                new Role("ASE 2", "TESTER"),

                // Business Analyst Roles
                new Role("BA 1", "BUSINESS_ANALYST"),
                new Role("BA 2", "BUSINESS_ANALYST"),

                // Default Role for Self-Signup
                new Role("TEAM_MEMBER", "TEAM_MEMBER")
        );
        roleRepository.saveAll(roles);
        System.out.println(roles.size() + " roles have been created.");
    }
}