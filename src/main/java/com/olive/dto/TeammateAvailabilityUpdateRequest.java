package com.olive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TeammateAvailabilityUpdateRequest {
    @NotBlank(message = "Availability status is required")
    @Pattern(regexp = "Free|Occupied|Available|Leave", message = "Availability status must be 'Free' or 'Occupied' or Available or Leave")
    private String availabilityStatus;

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
