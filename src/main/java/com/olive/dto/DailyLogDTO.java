package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class DailyLogDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private double totalHours;

    public DailyLogDTO(LocalDate date, double totalHours) {
        this.date = date;
        this.totalHours = totalHours;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }
}