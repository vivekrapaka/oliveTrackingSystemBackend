package com.olive.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryResponse {
    private long totalTeammates;
    private long freeTeammates;
    private long occupiedTeammates;
    private Map<String, Long> tasksByStage; // e.g., {"SIT": 5, "DEV": 10}
    private Map<String, Long> tasksByIssueType; // e.g., {"BRD": 7, "Production Issue": 3}
    private long tasksPendingCodeReview;
    private long tasksPendingCmcApproval;

    // New: Recent Tasks based on user's requested structure
    private List<DashboardTaskDTO> recentTasks;

    // New: Team Members Summary based on user's requested structure
    private List<DashboardTeammateDTO> teamMembersSummary;


    public DashboardSummaryResponse(long totalTeammates, long freeTeammates, long occupiedTeammates,
                                    Map<String, Long> tasksByStage, Map<String, Long> tasksByIssueType,
                                    long tasksPendingCodeReview, long tasksPendingCmcApproval,
                                    List<DashboardTaskDTO> recentTasks, List<DashboardTeammateDTO> teamMembersSummary) {
        this.totalTeammates = totalTeammates;
        this.freeTeammates = freeTeammates;
        this.occupiedTeammates = occupiedTeammates;
        this.tasksByStage = tasksByStage;
        this.tasksByIssueType = tasksByIssueType;
        this.tasksPendingCodeReview = tasksPendingCodeReview;
        this.tasksPendingCmcApproval = tasksPendingCmcApproval;
        this.recentTasks = recentTasks;
        this.teamMembersSummary = teamMembersSummary;
    }

    // Getters and Setters
    public long getTotalTeammates() {
        return totalTeammates;
    }

    public void setTotalTeammates(long totalTeammates) {
        this.totalTeammates = totalTeammates;
    }

    public long getFreeTeammates() {
        return freeTeammates;
    }

    public void setFreeTeammates(long freeTeammates) {
        this.freeTeammates = freeTeammates;
    }

    public long getOccupiedTeammates() {
        return occupiedTeammates;
    }

    public void setOccupiedTeammates(long occupiedTeammates) {
        this.occupiedTeammates = occupiedTeammates;
    }

    public Map<String, Long> getTasksByStage() {
        return tasksByStage;
    }

    public void setTasksByStage(Map<String, Long> tasksByStage) {
        this.tasksByStage = tasksByStage;
    }

    public Map<String, Long> getTasksByIssueType() {
        return tasksByIssueType;
    }

    public void setTasksByIssueType(Map<String, Long> tasksByIssueType) {
        this.tasksByIssueType = tasksByIssueType;
    }

    public long getTasksPendingCodeReview() {
        return tasksPendingCodeReview;
    }

    public void setTasksPendingCodeReview(long tasksPendingCodeReview) {
        this.tasksPendingCodeReview = tasksPendingCodeReview;
    }

    public long getTasksPendingCmcApproval() {
        return tasksPendingCmcApproval;
    }

    public void setTasksPendingCmcApproval(long tasksPendingCmcApproval) {
        this.tasksPendingCmcApproval = tasksPendingCmcApproval;
    }

    public List<DashboardTaskDTO> getRecentTasks() {
        return recentTasks;
    }

    public void setRecentTasks(List<DashboardTaskDTO> recentTasks) {
        this.recentTasks = recentTasks;
    }

    public List<DashboardTeammateDTO> getTeamMembersSummary() {
        return teamMembersSummary;
    }

    public void setTeamMembersSummary(List<DashboardTeammateDTO> teamMembersSummary) {
        this.teamMembersSummary = teamMembersSummary;
    }
}
