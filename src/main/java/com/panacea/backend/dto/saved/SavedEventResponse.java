package com.panacea.backend.dto.saved;

import com.panacea.backend.entity.SavedEvent;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedEventResponse {

    private Long id;

    private String eventId;

    private String name;

    private String category;

    private String type;

    private String date;

    private String time;

    private String venue;

    private String description;

    private LocalDateTime savedAt;

    public static SavedEventResponse from(
            SavedEvent event
    ) {
        return SavedEventResponse.builder()
                .id(event.getId())
                .eventId(event.getEventId())
                .name(event.getName())
                .category(event.getCategory())
                .type(event.getType())
                .date(event.getDate())
                .time(event.getTime())
                .venue(event.getVenue())
                .description(event.getDescription())
                .savedAt(event.getSavedAt())
                .build();
    }
}