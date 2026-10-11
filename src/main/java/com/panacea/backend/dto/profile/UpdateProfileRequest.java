package com.panacea.backend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank
        @Size(min = 2, max = 100)
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z .'-]{1,99}$",
                message = "Please enter a valid full name."
        )
        String fullName,

        @NotBlank
        String collegeName,

        @NotBlank
        @Size(max = 60)
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z .'-]{1,59}$",
                message = "Please enter a valid city."
        )
        String city,

        @NotBlank
        String state
) {}
