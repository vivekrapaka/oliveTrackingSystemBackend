package com.olive.dto;

public class TeammateResponse {
    private Long teammateId;
    private String name;
    private String email;
    private String availabilityStatus;

    public TeammateResponse(Long teammateId, String name, String email, String availabilityStatus) {
        this.teammateId = teammateId;
        this.name = name;
        this.email = email;
        this.availabilityStatus = availabilityStatus;
    }

    // Getters and Setters
    public Long getTeammateId() {
        return teammateId;
    }

    public void setTeammateId(Long teammateId) {
        this.teammateId = teammateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
