package com.olive.repository;

import com.olive.model.Teammate;
import com.olive.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeammateRepository extends JpaRepository<Teammate, Long> {

    Optional<Teammate> findByUser(User user);

    // FIX: Corrected method name from findByProjects_ProjectIdIn to findByProjects_IdIn
    // This correctly references the 'id' field of the Project entity.
    List<Teammate> findByProjects_IdIn(List<Long> projectIds);
    List<Teammate> findByProjects_IdIn(List<Long> projectIds, Sort sort);
}