package com.panacea.backend.service;

import com.panacea.backend.Entity.Event;
import com.panacea.backend.Exceptions.ResourceNotFoundException;
import com.panacea.backend.dto.admin.EventRequest;
import com.panacea.backend.dto.admin.EventResponse;
import com.panacea.backend.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminEventService {

    private final EventRepository eventRepository;

    public AdminEventService(
            EventRepository eventRepository
    ) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponse createEvent(
            EventRequest request
    ) {

        validateRequest(request);

        Event event = new Event();

        applyRequest(event, request);

        Event saved =
                eventRepository.save(event);

        return toResponse(saved);
    }

    @Transactional
    public EventResponse updateEvent(
            Long id,
            EventRequest request
    ) {

        validateRequest(request);

        Event event =
                eventRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Event not found with id: " + id
                                )
                        );

        applyRequest(event, request);

        Event updated =
                eventRepository.save(event);

        return toResponse(updated);
    }

    @Transactional
    public void deleteEvent(
            Long id
    ) {

        Event event =
                eventRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Event not found with id: " + id
                                )
                        );

        eventRepository.delete(event);
    }

    public EventResponse getEvent(
            Long id
    ) {

        Event event =
                eventRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Event not found with id: " + id
                                )
                        );

        return toResponse(event);
    }

    public List<EventResponse> getAllEvents() {

        return eventRepository
                .findAllByOrderByEventDateAscStartTimeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EventResponse> getActiveEvents() {

        return eventRepository
                .findAllByActiveTrueOrderByEventDateAscStartTimeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EventResponse> getEventsByDate(
            LocalDate date
    ) {

        return eventRepository
                .findAllByEventDateOrderByStartTimeAsc(date)
                .stream()
                .filter(Event::isActive)
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(
            EventRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Event request is required"
            );
        }

        if (isBlank(request.getName())) {
            throw new IllegalArgumentException(
                    "Event name is required"
            );
        }

        if (isBlank(request.getCategory())) {
            throw new IllegalArgumentException(
                    "Event category is required"
            );
        }

        if (request.getEventDate() == null) {
            throw new IllegalArgumentException(
                    "Event date is required"
            );
        }

        if (request.getStartTime() == null) {
            throw new IllegalArgumentException(
                    "Event start time is required"
            );
        }

        if (request.getEndTime() != null &&
                request.getEndTime()
                        .isBefore(request.getStartTime())) {

            throw new IllegalArgumentException(
                    "Event end time cannot be before start time"
            );
        }

        if (isBlank(request.getVenue())) {
            throw new IllegalArgumentException(
                    "Event venue is required"
            );
        }
    }

    private void applyRequest(
            Event event,
            EventRequest request
    ) {

        event.setName(
                request.getName().trim()
        );

        event.setDescription(
                trimToNull(
                        request.getDescription()
                )
        );

        event.setCategory(
                request.getCategory().trim()
        );

        event.setEventDate(
                request.getEventDate()
        );

        event.setStartTime(
                request.getStartTime()
        );

        event.setEndTime(
                request.getEndTime()
        );

        event.setVenue(
                request.getVenue().trim()
        );

        event.setCoordinator(
                trimToNull(
                        request.getCoordinator()
                )
        );

        event.setContactNumber(
                trimToNull(
                        request.getContactNumber()
                )
        );

        event.setActive(
                request.isActive()
        );

        event.setFeatured(
                request.isFeatured()
        );
    }

    private EventResponse toResponse(
            Event event
    ) {

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getCategory(),
                event.getEventDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getVenue(),
                event.getCoordinator(),
                event.getContactNumber(),
                event.isActive(),
                event.isFeatured(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    private boolean isBlank(
            String value
    ) {

        return value == null ||
                value.trim().isEmpty();
    }

    private String trimToNull(
            String value
    ) {

        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }
}