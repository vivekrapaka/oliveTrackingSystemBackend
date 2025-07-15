package com.olive.dto;

public class TimeLogBreakdownDTO {
    private double developmentHours;
    private Double developmentDueHours;
    private double testingHours;
    private Double testingDueHours;

    public TimeLogBreakdownDTO(double developmentHours, Double developmentDueHours, double testingHours, Double testingDueHours) {
        this.developmentHours = developmentHours;
        this.developmentDueHours = developmentDueHours;
        this.testingHours = testingHours;
        this.testingDueHours = testingDueHours;
    }

    public double getDevelopmentHours() { return developmentHours; }
    public void setDevelopmentHours(double developmentHours) { this.developmentHours = developmentHours; }
    public Double getDevelopmentDueHours() { return developmentDueHours; }
    public void setDevelopmentDueHours(Double developmentDueHours) { this.developmentDueHours = developmentDueHours; }
    public double getTestingHours() { return testingHours; }
    public void setTestingHours(double testingHours) { this.testingHours = testingHours; }
    public Double getTestingDueHours() { return testingDueHours; }
    public void setTestingDueHours(Double testingDueHours) { this.testingDueHours = testingDueHours; }
}
