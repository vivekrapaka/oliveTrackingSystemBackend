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

    // Getters and Setters
    public Long getTeammateId() { return teammateId; }
    public void setTeammateId(Long teammateId) { this.teammateId = teammateId; }
    public String getTeammateName() { return teammateName; }
    public void setTeammateName(String teammateName) { this.teammateName = teammateName; }
    public double getTotalHoursForPeriod() { return totalHoursForPeriod; }
    public void setTotalHoursForPeriod(double totalHoursForPeriod) { this.totalHoursForPeriod = totalHoursForPeriod; }
    public List<DailyLogDTO> getDailyLogs() { return dailyLogs; }
    public void setDailyLogs(List<DailyLogDTO> dailyLogs) { this.dailyLogs = dailyLogs; }
}
