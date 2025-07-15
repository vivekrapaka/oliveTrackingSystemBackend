package com.olive.dto;

public class TeammateEffortDTO {
    private String teammateName;
    private double hoursLogged;

    public TeammateEffortDTO(String teammateName, double hoursLogged) {
        this.teammateName = teammateName;
        this.hoursLogged = hoursLogged;
    }

    // Getters
    public String getTeammateName() { return teammateName; }
    public double getHoursLogged() { return hoursLogged; }
}
