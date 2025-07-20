package com.olive.dto;

import java.util.List;

public class TimesheetResponse {

    private Long teammateId;
    private String teammateName;
    private double totalHoursForPeriod;
    private List<DailyLogDTO> dailyLogs;

    public TimesheetResponse(Long teammateId, String teammateName, double totalHoursForPeriod, List<DailyLogDTO> dailyLogs) {
        this.teammateId = teammateId;
        this.teammateName = teammateName;
        this.totalHoursForPeriod = totalHoursForPeriod;
        this.dailyLogs = dailyLogs;
    }

    // Getters
    public Long getTeammateId() { return teammateId; }
    public String getTeammateName() { return teammateName; }
    public double getTotalHoursForPeriod() { return totalHoursForPeriod; }
    public List<DailyLogDTO> getDailyLogs() { return dailyLogs; }
}
