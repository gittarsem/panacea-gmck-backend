package com.panacea.backend.service;


import com.panacea.backend.Entity.FestivalSettings;
import com.panacea.backend.Exceptions.ResourceNotFoundException;
import com.panacea.backend.dto.admin.FestivalSettingsRequest;
import com.panacea.backend.dto.admin.FestivalSettingsResponse;
import com.panacea.backend.repository.FestivalSettingsRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AdminSettingsService {

    private static final long SETTINGS_ID = 1L;

    private final FestivalSettingsRepository settingsRepository;

    public AdminSettingsService(
            FestivalSettingsRepository settingsRepository
    ) {
        this.settingsRepository =
                settingsRepository;
    }

    @Transactional
    public FestivalSettingsResponse getSettings() {

        FestivalSettings settings =
                settingsRepository.findById(
                        SETTINGS_ID
                ).orElseGet(
                        this::createDefaultSettings
                );

        return toResponse(settings);
    }

    @Transactional
    public FestivalSettingsResponse updateSettings(
            FestivalSettingsRequest request
    ) {

        validateRequest(request);

        FestivalSettings settings =
                settingsRepository.findById(
                        SETTINGS_ID
                ).orElseGet(
                        this::createDefaultSettings
                );

        applyRequest(
                settings,
                request
        );

        FestivalSettings saved =
                settingsRepository.save(settings);

        return toResponse(saved);
    }

    private FestivalSettings createDefaultSettings() {

        FestivalSettings settings =
                new FestivalSettings();

        settings.setId(SETTINGS_ID);

        settings.setFestivalName(
                "PANACEA 2026"
        );

        settings.setTagline(
                "FOUR DAYS, ONE VIBE, INFINITE MEMORIES"
        );

        settings.setMainVenue(
                "GMC KATHUA COLLEGE CAMPUS"
        );

        settings.setSportsVenue(
                "DISTRICT SPORTS STADIUM, KATHUA"
        );

        settings.setAcademicVenue(
                "LECTURE HALL 1 & 2"
        );

        settings.setFestivalActive(true);

        settings.setRegistrationOpen(false);

        return settingsRepository.save(settings);
    }

    private void validateRequest(
            FestivalSettingsRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Settings request is required"
            );
        }

        if (isBlank(request.getFestivalName())) {
            throw new IllegalArgumentException(
                    "Festival name is required"
            );
        }
    }

    private void applyRequest(
            FestivalSettings settings,
            FestivalSettingsRequest request
    ) {

        settings.setFestivalName(
                request.getFestivalName().trim()
        );

        settings.setTagline(
                trimToNull(
                        request.getTagline()
                )
        );

        settings.setDescription(
                trimToNull(
                        request.getDescription()
                )
        );

        settings.setMainVenue(
                trimToNull(
                        request.getMainVenue()
                )
        );

        settings.setSportsVenue(
                trimToNull(
                        request.getSportsVenue()
                )
        );

        settings.setAcademicVenue(
                trimToNull(
                        request.getAcademicVenue()
                )
        );

        settings.setInstagram(
                trimToNull(
                        request.getInstagram()
                )
        );

        settings.setPrimaryContact(
                trimToNull(
                        request.getPrimaryContact()
                )
        );

        settings.setSecondaryContact(
                trimToNull(
                        request.getSecondaryContact()
                )
        );

        settings.setRegistrationOpen(
                request.isRegistrationOpen()
        );

        settings.setFestivalActive(
                request.isFestivalActive()
        );
    }

    private FestivalSettingsResponse toResponse(
            FestivalSettings settings
    ) {

        return new FestivalSettingsResponse(
                settings.getId(),
                settings.getFestivalName(),
                settings.getTagline(),
                settings.getDescription(),
                settings.getMainVenue(),
                settings.getSportsVenue(),
                settings.getAcademicVenue(),
                settings.getInstagram(),
                settings.getPrimaryContact(),
                settings.getSecondaryContact(),
                settings.isRegistrationOpen(),
                settings.isFestivalActive(),
                settings.getUpdatedAt()
        );
    }

    private boolean isBlank(
            String value
    ) {

        return value == null ||
                value.trim().isEmpty();
    }

    private String trimToNull(
            String value
    ) {

        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }
}
