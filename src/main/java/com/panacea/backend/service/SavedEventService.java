package com.panacea.backend.service;

import com.panacea.backend.dto.saved.SavedEventRequest;
import com.panacea.backend.dto.saved.SavedEventResponse;
import com.panacea.backend.entity.SavedEvent;
import com.panacea.backend.repository.SavedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedEventService {

    private final SavedEventRepository savedEventRepository;

    public List<SavedEventResponse> getSavedEvents(
            String userEmail
    ) {

        return savedEventRepository
                .findAllByUserEmailOrderBySavedAtDesc(userEmail)
                .stream()
                .map(SavedEventResponse::from)
                .toList();
    }

    public SavedEventResponse saveEvent(
            String userEmail,
            SavedEventRequest request
    ) {

        /*
         * Prevent duplicate saved events.
         */
        return savedEventRepository
                .findByUserEmailAndEventId(
                        userEmail,
                        request.getEventId()
                )
                .map(SavedEventResponse::from)
                .orElseGet(() -> {

                    SavedEvent event = SavedEvent.builder()
                            .userEmail(userEmail)
                            .eventId(request.getEventId())
                            .name(request.getName())
                            .category(request.getCategory())
                            .type(request.getType())
                            .date(request.getDate())
                            .time(request.getTime())
                            .venue(request.getVenue())
                            .description(request.getDescription())
                            .build();

                    return SavedEventResponse.from(
                            savedEventRepository.save(event)
                    );
                });
    }

    public boolean isSaved(
            String userEmail,
            String eventId
    ) {

        return savedEventRepository
                .existsByUserEmailAndEventId(
                        userEmail,
                        eventId
                );
    }

    @Transactional
    public void removeEvent(
            String userEmail,
            String eventId
    ) {

        savedEventRepository.deleteByUserEmailAndEventId(
                userEmail,
                eventId
        );
    }
}