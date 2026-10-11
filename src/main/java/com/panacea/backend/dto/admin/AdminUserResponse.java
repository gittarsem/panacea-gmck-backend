package com.panacea.backend.dto.admin;

import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.Enum.Role;

import java.util.Set;

public record AdminUserResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        String collegeName,
        String city,
        String state,
        Set<Role> roles
) {

    public static AdminUserResponse from(UserEntity user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCollegeName(),
                user.getCity(),
                user.getState(),
                user.getRoles()
        );
    }
}