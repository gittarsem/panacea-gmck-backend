package com.panacea.backend.repository;

import com.panacea.backend.entity.SavedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedEventRepository
        extends JpaRepository<SavedEvent, Long> {

    List<SavedEvent> findAllByUserEmailOrderBySavedAtDesc(
            String userEmail
    );

    Optional<SavedEvent> findByUserEmailAndEventId(
            String userEmail,
            String eventId
    );

    boolean existsByUserEmailAndEventId(
            String userEmail,
            String eventId
    );

    void deleteByUserEmailAndEventId(
            String userEmail,
            String eventId
    );
}