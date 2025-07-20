package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class DailyLogDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private double totalHours;
    private DailyEffortBreakdownDTO breakdown;

    public DailyLogDTO(LocalDate date, double totalHours, DailyEffortBreakdownDTO breakdown) {
        this.date = date;
        this.totalHours = totalHours;
        this.breakdown = breakdown;
    }

    // Getters
    public LocalDate getDate() { return date; }
    public double getTotalHours() { return totalHours; }
    public DailyEffortBreakdownDTO getBreakdown() { return breakdown; }
}
