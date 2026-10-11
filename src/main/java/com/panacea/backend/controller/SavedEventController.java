package com.panacea.backend.controller;

import com.panacea.backend.dto.saved.SavedEventRequest;
import com.panacea.backend.dto.saved.SavedEventResponse;
import com.panacea.backend.service.SavedEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-events")
@RequiredArgsConstructor
public class SavedEventController {

    private final SavedEventService savedEventService;

    /**
     * GET
     * /api/saved-events
     *
     * Returns all events saved by the logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<SavedEventResponse>> getSavedEvents(
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                savedEventService.getSavedEvents(userEmail)
        );
    }

    /**
     * POST
     * /api/saved-events
     *
     * Saves an event for the logged-in user.
     */
    @PostMapping
    public ResponseEntity<SavedEventResponse> saveEvent(
            Authentication authentication,
            @Valid @RequestBody SavedEventRequest request
    ) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                savedEventService.saveEvent(
                        userEmail,
                        request
                )
        );
    }

    /**
     * GET
     * /api/saved-events/{eventId}/exists
     *
     * Checks whether a particular event is saved.
     */
    @GetMapping("/{eventId}/exists")
    public ResponseEntity<Boolean> isSaved(
            Authentication authentication,
            @PathVariable String eventId
    ) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                savedEventService.isSaved(
                        userEmail,
                        eventId
                )
        );
    }

    /**
     * DELETE
     * /api/saved-events/{eventId}
     *
     * Removes the event from PANACEA saved schedule.
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> removeEvent(
            Authentication authentication,
            @PathVariable String eventId
    ) {

        String userEmail = authentication.getName();

        savedEventService.removeEvent(
                userEmail,
                eventId
        );

        return ResponseEntity.noContent().build();
    }
}