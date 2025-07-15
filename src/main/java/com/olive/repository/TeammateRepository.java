package com.olive.repository;

import com.olive.model.Teammate;
import com.olive.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeammateRepository extends JpaRepository<Teammate, Long> {

    Optional<Teammate> findByUser(User user);
    List<Teammate> findByProjects_IdIn(List<Long> projectIds, Sort sort);
    List<Teammate> findByProjects_IdInAndUser_Role_FunctionalGroupIn(List<Long> projectIds, Collection<String> functionalGroups, Sort sort);

    // NEW: Find all teammates for a single project.
    List<Teammate> findByProjects_Id(Long projectId);
}