package com.olive.dto;

import java.util.List;

public class DailyEffortBreakdownDTO {
    private double taskHours;
    private double generalHours;
    private List<TaskLogDetailDTO> taskDetails;

    public DailyEffortBreakdownDTO(double taskHours, double generalHours, List<TaskLogDetailDTO> taskDetails) {
        this.taskHours = taskHours;
        this.generalHours = generalHours;
        this.taskDetails = taskDetails;
    }

    // Getters
    public double getTaskHours() { return taskHours; }
    public double getGeneralHours() { return generalHours; }
    public List<TaskLogDetailDTO> getTaskDetails() { return taskDetails; }
}