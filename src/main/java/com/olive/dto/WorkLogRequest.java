package com.olive.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class WorkLogRequest {
    @NotNull(message = "Hours spent cannot be null")
    @Positive(message = "Hours spent must be positive")
    private Double hoursSpent;

    @NotNull(message = "Log date cannot be null")
    private LocalDate logDate;

    private String description;

    public Double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(Double hoursSpent) { this.hoursSpent = hoursSpent; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
