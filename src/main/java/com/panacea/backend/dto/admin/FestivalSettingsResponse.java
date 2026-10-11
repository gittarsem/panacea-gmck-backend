package com.panacea.backend.dto.admin;

import java.time.LocalDateTime;

public class FestivalSettingsResponse {

    private Long id;
    private String festivalName;
    private String tagline;
    private String description;
    private String mainVenue;
    private String sportsVenue;
    private String academicVenue;
    private String instagram;
    private String primaryContact;
    private String secondaryContact;
    private boolean registrationOpen;
    private boolean festivalActive;
    private LocalDateTime updatedAt;

    public FestivalSettingsResponse() {
    }

    public FestivalSettingsResponse(
            Long id,
            String festivalName,
            String tagline,
            String description,
            String mainVenue,
            String sportsVenue,
            String academicVenue,
            String instagram,
            String primaryContact,
            String secondaryContact,
            boolean registrationOpen,
            boolean festivalActive,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.festivalName = festivalName;
        this.tagline = tagline;
        this.description = description;
        this.mainVenue = mainVenue;
        this.sportsVenue = sportsVenue;
        this.academicVenue = academicVenue;
        this.instagram = instagram;
        this.primaryContact = primaryContact;
        this.secondaryContact = secondaryContact;
        this.registrationOpen = registrationOpen;
        this.festivalActive = festivalActive;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFestivalName() {
        return festivalName;
    }

    public String getTagline() {
        return tagline;
    }

    public String getDescription() {
        return description;
    }

    public String getMainVenue() {
        return mainVenue;
    }

    public String getSportsVenue() {
        return sportsVenue;
    }

    public String getAcademicVenue() {
        return academicVenue;
    }

    public String getInstagram() {
        return instagram;
    }

    public String getPrimaryContact() {
        return primaryContact;
    }

    public String getSecondaryContact() {
        return secondaryContact;
    }

    public boolean isRegistrationOpen() {
        return registrationOpen;
    }

    public boolean isFestivalActive() {
        return festivalActive;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
