package com.panacea.backend.controller;

import com.panacea.backend.dto.admin.ScheduleResponse;
import com.panacea.backend.service.AdminScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final AdminScheduleService adminScheduleService;

    public ScheduleController(
            AdminScheduleService adminScheduleService
    ) {
        this.adminScheduleService =
                adminScheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedule() {

        return ResponseEntity.ok(
                adminScheduleService.getActiveScheduleItems()
        );
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ScheduleResponse>> getScheduleByDate(
            @PathVariable LocalDate date
    ) {

        return ResponseEntity.ok(
                adminScheduleService.getScheduleByDate(date)
        );
    }
}