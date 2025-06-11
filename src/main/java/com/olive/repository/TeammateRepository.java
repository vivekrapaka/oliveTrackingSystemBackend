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
    Optional<Teammate> findByEmail(String Email);

}
