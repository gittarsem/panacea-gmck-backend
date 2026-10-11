package com.panacea.backend.controller;

import com.panacea.backend.dto.admin.EventResponse;
import com.panacea.backend.service.AdminEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final AdminEventService adminEventService;

    public EventController(
            AdminEventService adminEventService
    ) {
        this.adminEventService =
                adminEventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents() {

        return ResponseEntity.ok(
                adminEventService.getActiveEvents()
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

    @GetMapping("/date/{date}")
    public ResponseEntity<List<EventResponse>> getEventsByDate(
            @PathVariable LocalDate date
    ) {

        return ResponseEntity.ok(
                adminEventService.getEventsByDate(date)
        );
    }
}