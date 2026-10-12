package com.panacea.backend.controller;

import com.panacea.backend.dto.admin.FestivalSettingsRequest;
import com.panacea.backend.dto.admin.FestivalSettingsResponse;
import com.panacea.backend.service.AdminSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(
            AdminSettingsService adminSettingsService
    ) {
        this.adminSettingsService =
                adminSettingsService;
    }

    @GetMapping
    public ResponseEntity<FestivalSettingsResponse> getSettings() {

        return ResponseEntity.ok(
                adminSettingsService.getSettings()
        );
    }

    @PutMapping
    public ResponseEntity<FestivalSettingsResponse> updateSettings(
            @Valid @RequestBody FestivalSettingsRequest request
    ) {

        return ResponseEntity.ok(
                adminSettingsService.updateSettings(request)
        );
    }
}