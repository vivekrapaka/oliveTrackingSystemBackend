package com.olive.dto;

public class TimeLogBreakdownDTO {
    private double developmentHours;
    private double testingHours;
    private double otherHours;

    public TimeLogBreakdownDTO(double developmentHours, double testingHours, double otherHours) {
        this.developmentHours = developmentHours;
        this.testingHours = testingHours;
        this.otherHours = otherHours;
    }
    // Getters and Setters...

    public double getDevelopmentHours() {
        return developmentHours;
    }

    public void setDevelopmentHours(double developmentHours) {
        this.developmentHours = developmentHours;
    }

    public double getTestingHours() {
        return testingHours;
    }

    public void setTestingHours(double testingHours) {
        this.testingHours = testingHours;
    }

    public double getOtherHours() {
        return otherHours;
    }

    public void setOtherHours(double otherHours) {
        this.otherHours = otherHours;
    }
}