package com.olive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class DailyLogDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private double totalHours;
    private List<TaskLogDetailDTO> taskLogs; // NEW

    public DailyLogDTO(LocalDate date, double totalHours, List<TaskLogDetailDTO> taskLogs) {
        this.date = date;
        this.totalHours = totalHours;
        this.taskLogs = taskLogs;
    }

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

    public List<TaskLogDetailDTO> getTaskLogs() {
        return taskLogs;
    }

    public void setTaskLogs(List<TaskLogDetailDTO> taskLogs) {
        this.taskLogs = taskLogs;
    }
}