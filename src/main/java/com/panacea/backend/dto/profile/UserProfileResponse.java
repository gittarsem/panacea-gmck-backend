package com.panacea.backend.dto.profile;

import java.util.List;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        String collegeName,
        String city,
        String state,
        List<String> roles
) {}
