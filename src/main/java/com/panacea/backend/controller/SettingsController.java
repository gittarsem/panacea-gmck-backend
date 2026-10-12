package com.panacea.backend.controller;

import com.panacea.backend.dto.admin.FestivalSettingsResponse;
import com.panacea.backend.service.AdminSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final AdminSettingsService adminSettingsService;

    public SettingsController(
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
}