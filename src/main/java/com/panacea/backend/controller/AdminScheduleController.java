package com.panacea.backend.controller;

import com.panacea.backend.dto.admin.ScheduleRequest;
import com.panacea.backend.dto.admin.ScheduleResponse;
import com.panacea.backend.service.AdminScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/schedule")
@PreAuthorize("hasRole('ADMIN')")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    public AdminScheduleController(
            AdminScheduleService adminScheduleService
    ) {
        this.adminScheduleService =
                adminScheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllScheduleItems() {

        return ResponseEntity.ok(
                adminScheduleService.getAllScheduleItems()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleItem(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                adminScheduleService.getScheduleItem(id)
        );
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createScheduleItem(
            @Valid @RequestBody ScheduleRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminScheduleService
                                .createScheduleItem(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateScheduleItem(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request
    ) {

        return ResponseEntity.ok(
                adminScheduleService.updateScheduleItem(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheduleItem(
            @PathVariable Long id
    ) {

        adminScheduleService.deleteScheduleItem(id);

        return ResponseEntity.noContent().build();
    }
}