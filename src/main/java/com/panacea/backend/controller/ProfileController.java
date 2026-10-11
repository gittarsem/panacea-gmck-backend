package com.panacea.backend.controller;

import com.panacea.backend.dto.profile.ChangePasswordRequest;
import com.panacea.backend.dto.profile.UpdateProfileRequest;
import com.panacea.backend.dto.profile.UserProfileResponse;
import com.panacea.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                profileService.updateMyProfile(request)
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
