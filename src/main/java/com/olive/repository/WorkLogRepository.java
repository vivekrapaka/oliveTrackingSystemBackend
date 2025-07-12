package com.olive.repository;

import com.olive.model.Teammate;
import com.olive.model.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByTaskIdOrderByLogDateDesc(Long taskId);

    // FIX: Replaced the custom query with a standard derived query.
    // This method now finds all WorkLog entities by passing the actual Teammate object
    // and a date range, which is a more reliable way for Spring Data JPA to build the query.
    List<WorkLog> findByTeammateAndLogDateBetween(Teammate teammate, LocalDate startDate, LocalDate endDate);
}
