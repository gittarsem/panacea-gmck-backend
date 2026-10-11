package com.panacea.backend.service;

import com.panacea.backend.Entity.ScheduleItem;
import com.panacea.backend.Exceptions.ResourceNotFoundException;
import com.panacea.backend.dto.admin.ScheduleRequest;
import com.panacea.backend.dto.admin.ScheduleResponse;
import com.panacea.backend.repository.ScheduleItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminScheduleService {

    private final ScheduleItemRepository scheduleItemRepository;

    public AdminScheduleService(
            ScheduleItemRepository scheduleItemRepository
    ) {
        this.scheduleItemRepository =
                scheduleItemRepository;
    }

    @Transactional
    public ScheduleResponse createScheduleItem(
            ScheduleRequest request
    ) {

        validateRequest(request);

        ScheduleItem item =
                new ScheduleItem();

        applyRequest(
                item,
                request
        );

        ScheduleItem saved =
                scheduleItemRepository.save(item);

        return toResponse(saved);
    }

    @Transactional
    public ScheduleResponse updateScheduleItem(
            Long id,
            ScheduleRequest request
    ) {

        validateRequest(request);

        ScheduleItem item =
                scheduleItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Schedule item not found with id: " + id
                                )
                        );

        applyRequest(
                item,
                request
        );

        ScheduleItem updated =
                scheduleItemRepository.save(item);

        return toResponse(updated);
    }

    @Transactional
    public void deleteScheduleItem(
            Long id
    ) {

        ScheduleItem item =
                scheduleItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Schedule item not found with id: " + id
                                )
                        );

        scheduleItemRepository.delete(item);
    }

    public ScheduleResponse getScheduleItem(
            Long id
    ) {

        ScheduleItem item =
                scheduleItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Schedule item not found with id: " + id
                                )
                        );

        return toResponse(item);
    }

    public List<ScheduleResponse> getAllScheduleItems() {

        return scheduleItemRepository
                .findAllByOrderByScheduleDateAscStartTimeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ScheduleResponse> getActiveScheduleItems() {

        return scheduleItemRepository
                .findAllByActiveTrueOrderByScheduleDateAscStartTimeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ScheduleResponse> getScheduleByDate(
            LocalDate date
    ) {

        return scheduleItemRepository
                .findAllByScheduleDateOrderByStartTimeAsc(date)
                .stream()
                .filter(ScheduleItem::isActive)
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(
            ScheduleRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Schedule request is required"
            );
        }

        if (isBlank(request.getTitle())) {
            throw new IllegalArgumentException(
                    "Schedule title is required"
            );
        }

        if (request.getScheduleDate() == null) {
            throw new IllegalArgumentException(
                    "Schedule date is required"
            );
        }

        if (request.getStartTime() == null) {
            throw new IllegalArgumentException(
                    "Schedule start time is required"
            );
        }

        if (request.getEndTime() != null &&
                request.getEndTime()
                        .isBefore(request.getStartTime())) {

            throw new IllegalArgumentException(
                    "Schedule end time cannot be before start time"
            );
        }

        if (isBlank(request.getVenue())) {
            throw new IllegalArgumentException(
                    "Schedule venue is required"
            );
        }
    }

    private void applyRequest(
            ScheduleItem item,
            ScheduleRequest request
    ) {

        item.setTitle(
                request.getTitle().trim()
        );

        item.setDescription(
                trimToNull(
                        request.getDescription()
                )
        );

        item.setScheduleDate(
                request.getScheduleDate()
        );

        item.setStartTime(
                request.getStartTime()
        );

        item.setEndTime(
                request.getEndTime()
        );

        item.setVenue(
                request.getVenue().trim()
        );

        item.setCategory(
                trimToNull(
                        request.getCategory()
                )
        );

        item.setActive(
                request.isActive()
        );
    }

    private ScheduleResponse toResponse(
            ScheduleItem item
    ) {

        return new ScheduleResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getScheduleDate(),
                item.getStartTime(),
                item.getEndTime(),
                item.getVenue(),
                item.getCategory(),
                item.isActive(),
                item.getCreatedAt(),
                item.getUpdatedAt()
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