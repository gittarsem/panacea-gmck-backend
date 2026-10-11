package com.panacea.backend.repository;


import com.panacea.backend.Entity.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleItemRepository
        extends JpaRepository<ScheduleItem, Long> {

    List<ScheduleItem>
    findAllByOrderByScheduleDateAscStartTimeAsc();

    List<ScheduleItem>
    findAllByActiveTrueOrderByScheduleDateAscStartTimeAsc();

    List<ScheduleItem>
    findAllByScheduleDateOrderByStartTimeAsc(
            LocalDate scheduleDate
    );

    long countByActiveTrue();
}
