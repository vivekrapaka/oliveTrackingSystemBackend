package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class WorkLogResponse {
    private Long id;
    private Double hoursSpent;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logDate;
    private String description;
    private Long teammateId;
    private String teammateName;

    public WorkLogResponse(Long id, Double hoursSpent, LocalDate logDate, String description, Long teammateId, String teammateName) {
        this.id = id;
        this.hoursSpent = hoursSpent;
        this.logDate = logDate;
        this.description = description;
        this.teammateId = teammateId;
        this.teammateName = teammateName;
    }
    // Getters...

    public Long getId() {
        return id;
    }

    public Double getHoursSpent() {
        return hoursSpent;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public String getDescription() {
        return description;
    }

    public Long getTeammateId() {
        return teammateId;
    }

    public String getTeammateName() {
        return teammateName;
    }
}