package com.panacea.backend.dto.saved;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedEventRequest {

    @NotBlank
    private String eventId;

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotBlank
    private String type;

    @NotBlank
    private String date;

    @NotBlank
    private String time;

    @NotBlank
    private String venue;

    private String description;
}