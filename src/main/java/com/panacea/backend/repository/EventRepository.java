package com.panacea.backend.repository;


import com.panacea.backend.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByEventDateAscStartTimeAsc();

    List<Event> findAllByActiveTrueOrderByEventDateAscStartTimeAsc();

    List<Event> findAllByEventDateOrderByStartTimeAsc(
            LocalDate eventDate
    );

    List<Event> findAllByCategoryIgnoreCaseOrderByEventDateAscStartTimeAsc(
            String category
    );

    long countByActiveTrue();

    long countByEventDate(LocalDate eventDate);
}
