package com.panacea.backend.service;

import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.dto.profile.ChangePasswordRequest;
import com.panacea.backend.dto.profile.UpdateProfileRequest;
import com.panacea.backend.dto.profile.UserProfileResponse;
import com.panacea.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UserEntity currentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {
            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User account not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        return toResponse(currentUser());
    }

    @Transactional
    public UserProfileResponse updateMyProfile(
            UpdateProfileRequest request
    ) {
        UserEntity user = currentUser();

        String fullName = request.fullName().trim();
        String collegeName = request.collegeName().trim();
        String city = request.city().trim();
        String state = request.state().trim();

        if (!fullName.matches(
                "^[A-Za-z][A-Za-z .'-]{1,99}$"
        )) {
            throw new IllegalArgumentException(
                    "Please enter a valid full name."
            );
        }

        if (collegeName.isBlank()) {
            throw new IllegalArgumentException(
                    "College is required."
            );
        }

        if (!city.matches(
                "^[A-Za-z][A-Za-z .'-]{1,59}$"
        )) {
            throw new IllegalArgumentException(
                    "Please enter a valid city."
            );
        }

        if (state.isBlank()) {
            throw new IllegalArgumentException(
                    "State is required."
            );
        }

        /*
         * Intentionally do NOT update email or phone number.
         * They remain immutable through this endpoint.
         */
        user.setFullName(fullName);
        user.setCollegeName(collegeName);
        user.setCity(city);
        user.setState(state);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(
            ChangePasswordRequest request
    ) {
        UserEntity user = currentUser();

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }

        if (request.newPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "New password must be at least 8 characters."
            );
        }

        if (!request.newPassword().matches(
                ".*[A-Za-z].*"
        )) {
            throw new IllegalArgumentException(
                    "New password must contain at least one letter."
            );
        }

        if (!request.newPassword().matches(
                ".*\\d.*"
        )) {
            throw new IllegalArgumentException(
                    "New password must contain at least one number."
            );
        }

        if (!request.newPassword().equals(
                request.confirmPassword()
        )) {
            throw new IllegalArgumentException(
                    "New passwords do not match."
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "New password must be different from your current password."
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        userRepository.save(user);
    }

    private UserProfileResponse toResponse(UserEntity user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCollegeName(),
                user.getCity(),
                user.getState(),
                null
        );
    }
}
