package com.panacea.backend.dto.admin;


public class FestivalSettingsRequest {

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

    public String getFestivalName() {
        return festivalName;
    }

    public void setFestivalName(String festivalName) {
        this.festivalName = festivalName;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainVenue() {
        return mainVenue;
    }

    public void setMainVenue(String mainVenue) {
        this.mainVenue = mainVenue;
    }

    public String getSportsVenue() {
        return sportsVenue;
    }

    public void setSportsVenue(String sportsVenue) {
        this.sportsVenue = sportsVenue;
    }

    public String getAcademicVenue() {
        return academicVenue;
    }

    public void setAcademicVenue(String academicVenue) {
        this.academicVenue = academicVenue;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(String primaryContact) {
        this.primaryContact = primaryContact;
    }

    public String getSecondaryContact() {
        return secondaryContact;
    }

    public void setSecondaryContact(String secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    public boolean isRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(boolean registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public boolean isFestivalActive() {
        return festivalActive;
    }

    public void setFestivalActive(boolean festivalActive) {
        this.festivalActive = festivalActive;
    }
}
