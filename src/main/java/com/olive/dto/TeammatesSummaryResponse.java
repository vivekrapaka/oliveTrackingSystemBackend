package com.olive.dto;

import java.util.List;

public class TeammatesSummaryResponse {
    private long totalMembersInTeamCount;
    private long availableTeamMembersCount;
    private long occupiedTeamMembersCount;
    private long activeTasksCount; // Global count of active tasks (not just assigned to team members)
    private List<TeammateResponse> teammates; // List of individual teammate details

    public TeammatesSummaryResponse(long totalMembersInTeamCount, long availableTeamMembersCount, long occupiedTeamMembersCount, long activeTasksCount, List<TeammateResponse> teammates) {
        this.totalMembersInTeamCount = totalMembersInTeamCount;
        this.availableTeamMembersCount = availableTeamMembersCount;
        this.occupiedTeamMembersCount = occupiedTeamMembersCount;
        this.activeTasksCount = activeTasksCount;
        this.teammates = teammates;
    }

    // Getters and Setters
    public long getTotalMembersInTeamCount() {
        return totalMembersInTeamCount;
    }

    public void setTotalMembersInTeamCount(long totalMembersInTeamCount) {
        this.totalMembersInTeamCount = totalMembersInTeamCount;
    }

    public long getAvailableTeamMembersCount() {
        return availableTeamMembersCount;
    }

    public void setAvailableTeamMembersCount(long availableTeamMembersCount) {
        this.availableTeamMembersCount = availableTeamMembersCount;
    }

    public long getOccupiedTeamMembersCount() {
        return occupiedTeamMembersCount;
    }

    public void setOccupiedTeamMembersCount(long occupiedTeamMembersCount) {
        this.occupiedTeamMembersCount = occupiedTeamMembersCount;
    }

    public long getActiveTasksCount() {
        return activeTasksCount;
    }

    public void setActiveTasksCount(long activeTasksCount) {
        this.activeTasksCount = activeTasksCount;
    }

    public List<TeammateResponse> getTeammates() {
        return teammates;
    }

    public void setTeammates(List<TeammateResponse> teammates) {
        this.teammates = teammates;
    }
}
