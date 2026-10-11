package com.panacea.backend.controller;

import com.panacea.backend.dto.admin.EventRequest;
import com.panacea.backend.dto.admin.EventResponse;
import com.panacea.backend.service.AdminEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final AdminEventService adminEventService;

    public AdminEventController(
            AdminEventService adminEventService
    ) {
        this.adminEventService =
                adminEventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {

        return ResponseEntity.ok(
                adminEventService.getAllEvents()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                adminEventService.getEvent(id)
        );
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminEventService.createEvent(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request
    ) {

        return ResponseEntity.ok(
                adminEventService.updateEvent(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id
    ) {

        adminEventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }
}