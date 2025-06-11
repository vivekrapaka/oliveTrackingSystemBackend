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

    // Optional: List of active assignments for a quick glance on dashboard
    private List<AssignmentSummaryDTO> activeAssignments;

    public DashboardSummaryResponse(long totalTeammates, long freeTeammates, long occupiedTeammates,
                                    Map<String, Long> tasksByStage, Map<String, Long> tasksByIssueType,
                                    long tasksPendingCodeReview, long tasksPendingCmcApproval,
                                    List<AssignmentSummaryDTO> activeAssignments) {
        this.totalTeammates = totalTeammates;
        this.freeTeammates = freeTeammates;
        this.occupiedTeammates = occupiedTeammates;
        this.tasksByStage = tasksByStage;
        this.tasksByIssueType = tasksByIssueType;
        this.tasksPendingCodeReview = tasksPendingCodeReview;
        this.tasksPendingCmcApproval = tasksPendingCmcApproval;
        this.activeAssignments = activeAssignments;
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

    public List<AssignmentSummaryDTO> getActiveAssignments() {
        return activeAssignments;
    }

    public void setActiveAssignments(List<AssignmentSummaryDTO> activeAssignments) {
        this.activeAssignments = activeAssignments;
    }
}
