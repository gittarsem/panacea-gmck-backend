package com.panacea.backend.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "festival_settings")
public class FestivalSettings {

    @Id
    private Long id;

    @Column(nullable = false, length = 150)
    private String festivalName;

    @Column(length = 500)
    private String tagline;

    @Column(length = 500)
    private String description;

    @Column(length = 200)
    private String mainVenue;

    @Column(length = 200)
    private String sportsVenue;

    @Column(length = 200)
    private String academicVenue;

    @Column(length = 100)
    private String instagram;

    @Column(length = 30)
    private String primaryContact;

    @Column(length = 30)
    private String secondaryContact;

    @Column(nullable = false)
    private boolean registrationOpen = false;

    @Column(nullable = false)
    private boolean festivalActive = true;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = 1L;
        }

        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
