package com.olive.repository;

import com.olive.model.Teammate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeammateRepository extends JpaRepository<Teammate,Long> {
    // Custom query to find teammates by availability status
    List<Teammate> findByAvailabilityStatus(String availabilityStatus);
    Optional<Teammate> findByName(String name);
    Optional<Teammate> findByEmail(String email);
    Optional<Teammate> findByNameIgnoreCase(String name);

    // UPDATED: Find teammates by project ID (replaces findByTeamId)
    List<Teammate> findByProjectId(Long projectId);

    // NEW: Find teammate by name and projectId
    Optional<Teammate> findByNameIgnoreCaseAndProjectId(String name, Long projectId);

    // NEW: Find teammate by email and projectId
    Optional<Teammate> findByEmailAndProjectId(String email, Long projectId);
}
